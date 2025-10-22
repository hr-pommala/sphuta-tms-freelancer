package net.sphuta.tms.freelancer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.dto.Item;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.repository.InvoiceRepository;
import net.sphuta.tms.freelancer.util.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ======================================================================
 * ReportService
 * ======================================================================
 * Service class responsible for generating invoice PDFs,
 * sending them via email, and handling batch processing of invoices.
 *
 * ✅ Scheduler-friendly method to process multiple invoices
 * ✅ Reuses existing PDF generation and email logic
 * ✅ Robust error handling and logging
 * ======================================================================
 */

@Slf4j
@Service
public class ReportService {

    @Autowired
    private InvoiceRepository invoiceRepository;


    @Autowired(required = false)
    private EmailService emailService;

    /**
     * ======================================================================
     * Scheduler-friendly method
     * ======================================================================
     * Fetches all DRAFT invoices from DB, generates PDFs, sends emails,
     * and updates invoice status to SENT.
     *
     * @param scheduleType Name of the schedule (for logging)
     */
    public void generateAndSendReport(String scheduleType) {
        log.info("Scheduler '{}' started", scheduleType);

        List<Invoice> invoices = invoiceRepository.findByStatus("DRAFT");
        if (invoices.isEmpty()) {
            log.info("No invoices to process for '{}'", scheduleType);
            return;
        }

        for (Invoice invoice : invoices) {
            try {
                // Map entity → DTO
                InvoicePayload payload = InvoiceMapper.toPayload(invoice);

                // Generate PDF & send email
                generateInvoicePdf(payload);

                // Update invoice status
                invoice.setStatus("SENT");
                invoiceRepository.save(invoice);

                log.info("Invoice '{}' processed successfully", invoice.getInvoiceNo());

            } catch (Exception e) {
                log.error("Error processing invoice '{}': {}", invoice.getInvoiceNo(), e.getMessage(), e);
            }
        }

        log.info("Scheduler '{}' completed", scheduleType);
    }

    /**
     * ======================================================================
     * Generate single invoice PDF (existing logic)
     * ======================================================================
     * Generates a PDF invoice from the provided payload,
     * sends it via email, and returns the PDF in the HTTP response.
     * @param payload InvoicePayload containing all invoice details
     * @return ResponseEntity with PDF byte array or error message
     */
    public ResponseEntity<?> generateInvoicePdf(InvoicePayload payload) {
        try {
            log.info("Generating dynamic invoice PDF for client: {}", payload.clientName());

            // Load JRXML template from resources
            InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
            if (jrxml == null) {
                log.error("JRXML template not found at /reports/invoice.jrxml");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "invoice.jrxml template not found"));
            }

            // Compile the JasperReport from JRXML
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

            // Calculate totals
            List<Item> items = payload.items() != null ? payload.items() : List.of();
            BigDecimal subtotal = items.stream()
                    .map(i -> i.amount() != null ? i.amount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal taxRate = new BigDecimal("5.00");
            BigDecimal totalTax = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
            BigDecimal total = subtotal.add(totalTax);

            // Prepare parameters for JasperReport
            Map<String, Object> params = new HashMap<>();
            params.put("fromCompany", payload.fromCompany());
            params.put("fromAddress", payload.fromAddress());
            params.put("fromPhone", payload.fromPhone());
            params.put("fromEmail", payload.fromEmail());

            params.put("clientCompany", payload.clientName());
            params.put("contactName", payload.clientName());
            params.put("clientAddress", payload.clientAddress());
            params.put("clientPhone", payload.clientPhone());

            params.put("invoiceNo", payload.invoiceNo());
            params.put("invoiceDate", payload.invoiceDate());
            params.put("dueDate", payload.dueDate());

            params.put("bankName", payload.bankName());
            params.put("accountNo", payload.accountNo());

            params.put("subtotal", subtotal);
            params.put("taxRate", taxRate);
            params.put("totalTax", totalTax);
            params.put("total", total);

            // Add item data source
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            log.info("Invoice PDF generated successfully, size={} bytes", pdf.length);

            sendInvoiceByEmail(payload, pdf);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename((payload.invoiceNo() != null ? payload.invoiceNo() : "invoice") + ".pdf")
                            .build()
            );

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            log.error("JasperReports error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice PDF", "detail", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while generating invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected server error", "detail", e.getMessage()));
        }
    }

    /**
     * ======================================================================
     * Send email (unchanged)
     * ======================================================================
     * Sends the generated invoice PDF via email using EmailService.
     * @param payload InvoicePayload containing email metadata
     * @param pdf     byte[] array of the generated PDF
     */
    private void sendInvoiceByEmail(InvoicePayload payload, byte[] pdf) {
        if (emailService == null) {
            log.warn("[DEV MODE] EmailService not configured. Simulating email send to {}", payload.emailTo());
            System.out.println("=== Simulated Email ===");
            System.out.println("To: " + payload.emailTo());
            System.out.println("Subject: " + payload.emailSubject());
            System.out.println("Body: " + payload.emailBody());
            System.out.println("Attachment size: " + pdf.length + " bytes");
            return;
        }

        // Validate email address
        if (payload.emailTo() == null || payload.emailTo().isBlank()) {
            log.warn("Email address missing. Skipping email send.");
            return;
        }

        String subject = (payload.emailSubject() == null || payload.emailSubject().isBlank())
                ? "Invoice " + payload.invoiceNo()
                : payload.emailSubject();

        String body = (payload.emailBody() == null || payload.emailBody().isBlank())
                ? "Please find your invoice attached."
                : payload.emailBody();

        String fileName = (payload.invoiceNo() != null && !payload.invoiceNo().isBlank())
                ? payload.invoiceNo() + ".pdf"
                : "invoice.pdf";

        try {
            emailService.sendWithAttachment(payload.emailTo(), subject, body, pdf, fileName);
            log.info("Invoice emailed successfully to {}", payload.emailTo());
        } catch (Exception e) {
            log.error("Error sending invoice email to {}: {}", payload.emailTo(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}
