package net.sphuta.tms.freelancer.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.dto.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ======================================================================
 * ReportService
 * ======================================================================
 * This service handles dynamic PDF report generation using JasperReports.
 *
 * ✅ Primary Use:
 * - Generates invoices based on input data from InvoicePayload DTO.
 * - Optionally sends the generated PDF to the client via email.
 *
 * ✅ Features:
 * - Loads and compiles JasperReports JRXML templates.
 * - Dynamically fills report parameters from DTO fields.
 * - Calculates financial totals (subtotal, tax, total).
 * - Exports the filled report to a PDF byte array.
 * - Sends email (if enabled and EmailService is configured).
 *
 * ✅ Integration Points:
 * - Used by ReportController (/reports/invoice).
 * - Optionally integrates with EmailService.
 */
@Slf4j
@Service
public class ReportService {

    /**
     * EmailService dependency.
     *
     * Used to send the generated PDF invoice via email.
     * The 'required=false' ensures that the application runs even
     * if EmailService is not configured (e.g., in local dev mode).
     */
    @Autowired(required = false)
    private EmailService emailService;

    /**
     * ======================================================================
     * Method: generateInvoicePdf
     * ======================================================================
     * Generates a dynamic invoice PDF using JasperReports.
     *
     * Workflow:
     * 1️⃣ Load and compile Jasper template (.jrxml)
     * 2️⃣ Populate report parameters from InvoicePayload
     * 3️⃣ Calculate subtotal, tax, and total
     * 4️⃣ Export report to PDF byte array
     * 5️⃣ Send via email (if 'sendEmail' flag is true)
     * 6️⃣ Return PDF as HTTP ResponseEntity
     *
     * @param payload  DTO containing all invoice data
     * @return ResponseEntity containing PDF bytes or error message
     */
    public ResponseEntity<?> generateInvoicePdf(InvoicePayload payload) {
        try {
            log.info("Generating dynamic invoice PDF for client: {}", payload.clientName());

            // Step 1: Load the Jasper template file from resources folder
            InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
            if (jrxml == null) {
                // Template not found → return error response
                log.error("JRXML template not found at /reports/invoice.jrxml");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "invoice.jrxml template not found"));
            }

            // Step 2: Compile JRXML template into a JasperReport object
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

            // Step 3: Calculate totals (subtotal, tax, total)
            // ------------------------------------------------------------------
            List<Item> items = payload.items() != null ? payload.items() : List.of();
            BigDecimal subtotal = items.stream()
                    .map(i -> i.amount() != null ? i.amount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal taxRate = new BigDecimal("5.00"); // Static 5% tax
            BigDecimal totalTax = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
            BigDecimal total = subtotal.add(totalTax);
            // ------------------------------------------------------------------

            // Step 4: Prepare Jasper parameters (mapped to JRXML fields)
            Map<String, Object> params = new HashMap<>();

            // Company (sender) details
            params.put("fromCompany", payload.fromCompany());
            params.put("fromAddress", payload.fromAddress());
            params.put("fromPhone", payload.fromPhone());
            params.put("fromEmail", payload.fromEmail());

            // Client (receiver) details
            params.put("clientCompany", payload.clientName());
            params.put("contactName", payload.clientName());
            params.put("clientAddress", payload.clientAddress());
            params.put("clientPhone", payload.clientPhone());

            // Invoice details
            params.put("invoiceNo", payload.invoiceNo());
            params.put("invoiceDate", payload.invoiceDate());
            params.put("dueDate", payload.dueDate());

            // Bank/payment details
            params.put("bankName", payload.bankName());
            params.put("accountNo", payload.accountNo());

            // Calculated totals
            params.put("subtotal", subtotal);
            params.put("taxRate", taxRate);
            params.put("totalTax", totalTax);
            params.put("total", total);

            // Step 5: Fill Jasper report and export to PDF
            // JREmptyDataSource → report uses only parameters (no DB connection)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            log.info("Invoice PDF generated successfully, size={} bytes", pdf.length);

            // Step 6: If email sending is enabled, trigger email send
            if (Boolean.TRUE.equals(payload.sendEmail())) {
                sendInvoiceByEmail(payload, pdf);
            }

            // Step 7: Prepare response headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename((payload.invoiceNo() != null ? payload.invoiceNo() : "invoice") + ".pdf")
                            .build()
            );

            // Return the generated PDF as HTTP response
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            // JasperReports-specific exception
            log.error("JasperReports error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice PDF", "detail", e.getMessage()));

        } catch (Exception e) {
            // Any other unexpected errors
            log.error("Unexpected error while generating invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected server error", "detail", e.getMessage()));
        }
    }

    /**
     * ======================================================================
     * Method: sendInvoiceByEmail
     * ======================================================================
     * Sends the generated invoice PDF via email using EmailService.
     *
     * Behaviors:
     * - Skips email sending if EmailService is not available (dev mode)
     * - Skips if recipient email is missing
     * - Builds email subject, body, and attachment filename dynamically
     *
     * @param payload The InvoicePayload containing email details
     * @param pdf     The generated invoice PDF in byte[] format
     */
    private void sendInvoiceByEmail(InvoicePayload payload, byte[] pdf) {
        // If EmailService bean is not available (e.g., local development)
        if (emailService == null) {
            log.warn("[DEV MODE] EmailService not configured. Simulating email send to {}", payload.emailTo());
            System.out.println("=== Simulated Email ===");
            System.out.println("To: " + payload.emailTo());
            System.out.println("Subject: " + payload.emailSubject());
            System.out.println("Body: " + payload.emailBody());
            System.out.println("Attachment size: " + pdf.length + " bytes");
            return;
        }

        // Skip sending if no recipient email provided
        if (payload.emailTo() == null || payload.emailTo().isBlank()) {
            log.warn("Email address missing. Skipping email send.");
            return;
        }

        // Build email subject (use default if blank)
        String subject = (payload.emailSubject() == null || payload.emailSubject().isBlank())
                ? "Invoice " + payload.invoiceNo()
                : payload.emailSubject();

        // Build email body (use default message if blank)
        String body = (payload.emailBody() == null || payload.emailBody().isBlank())
                ? "Please find your invoice attached."
                : payload.emailBody();

        // Prepare file name for attachment
        String fileName = (payload.invoiceNo() != null && !payload.invoiceNo().isBlank())
                ? payload.invoiceNo() + ".pdf"
                : "invoice.pdf";

        try {
            // Send the email with PDF attachment using EmailService
            emailService.sendWithAttachment(payload.emailTo(), subject, body, pdf, fileName);
            log.info("Invoice emailed successfully to {}", payload.emailTo());
        } catch (Exception e) {
            // Log and rethrow exception if email sending fails
            log.error("Error sending invoice email to {}: {}", payload.emailTo(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

}
