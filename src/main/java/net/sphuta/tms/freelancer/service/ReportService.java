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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportService - generates invoice PDFs using JasperReports and optionally
 * sends them via EmailService when requested in the InvoicePayload.
 */
@Slf4j
@Service
public class ReportService {

    @Autowired(required = false)
    private EmailService emailService; // optional - your EmailService handles dev fallback

    // -----------------------
    // Endpoint helper methods
    // -----------------------

    /**
     * Build response for GET /reports/invoice (sample invoice download).
     */
    public ResponseEntity<byte[]> buildSampleInvoiceResponse() {
        try {
            byte[] pdf = generateSampleInvoice();
            log.debug("Sample invoice generated successfully. Size: {} bytes", (pdf != null ? pdf.length : 0));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("invoice.pdf").build());

            log.info("Sending generated sample invoice as response...");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            log.error("JasperReports exception while generating sample invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error generating sample invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Build response for POST /reports/invoice.
     *
     * If payload.sendEmail == true, will attempt to email the PDF to payload.emailTo.
     * Returns PDF (application/pdf) on success, or JSON error (400/500) on failure.
     */
    public ResponseEntity<?> buildInvoiceResponse(InvoicePayload payload) {
        boolean sendEmail = Boolean.TRUE.equals(payload.sendEmail());
        String recipientEmail = payload.emailTo();
        String subject = payload.emailSubject();
        String body = payload.emailBody();

        try {
            // Generate PDF bytes (core logic reused)
            byte[] pdf = generateInvoice(
                    payload.fromCompany(),
                    payload.fromAddress(),
                    payload.fromPhone(),
                    payload.fromEmail(),
                    payload.clientName(),
                    payload.clientAddress(),
                    payload.clientPhone(),
                    payload.invoiceNo(),
                    payload.invoiceDate(),
                    payload.dueDate(),
                    payload.bankName(),
                    payload.accountNo(),
                    payload.items()
            );

            log.debug("Custom invoice generated successfully. Size: {} bytes", (pdf != null ? pdf.length : 0));

            // If emailing requested, validate and send
            if (sendEmail) {
                if (recipientEmail == null || recipientEmail.isBlank()) {
                    log.warn("sendEmail requested but emailTo is missing in payload");
                    return ResponseEntity.badRequest().body(Map.of("error", "emailTo is required when sendEmail=true"));
                }

                String mailSubject = (subject == null || subject.isBlank()) ? ("Invoice " + (payload.invoiceNo() != null ? payload.invoiceNo() : "")) : subject;
                String mailBody = (body == null || body.isBlank()) ? ("Please find attached your invoice " + (payload.invoiceNo() != null ? payload.invoiceNo() : "")) : body;
                String filename = (payload.invoiceNo() != null && !payload.invoiceNo().isBlank()) ? payload.invoiceNo() + ".pdf" : "invoice.pdf";

                try {
                    if (emailService != null) {
                        emailService.sendWithAttachment(recipientEmail, mailSubject, mailBody, pdf, filename);
                    } else {
                        // emailService not configured — dev fallback: log & simulate
                        log.info("[DEV MODE] emailService not configured - simulated email to {}", recipientEmail);
                        System.out.println("=== Simulated send (dev) ===\nTo: " + recipientEmail + "\nSubject: " + mailSubject + "\nBody: " + mailBody + "\nAttachment: " + filename + " (" + (pdf != null ? pdf.length : 0) + " bytes)");
                    }
                } catch (Exception e) {
                    log.error("Failed to send invoice to {}: {}", recipientEmail, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "email sending failed", "detail", e.getMessage()));
                }
            }

            // Always return PDF as before
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("invoice_custom.pdf").build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            log.error("JasperReports exception while generating custom invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "pdf generation failed", "detail", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error generating custom invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "unexpected error", "detail", e.getMessage()));
        }
    }

    // -----------------------
    // Core generation methods (unchanged functionality)
    // -----------------------

    public byte[] generateInvoicePdf(Integer invoiceId) throws JRException {
        log.info("Request to generate invoice PDF for invoiceId={}", invoiceId);

        InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
        if (jrxml == null) {
            log.error("JRXML template not found at /reports/invoice.jrxml");
            throw new RuntimeException("invoice.jrxml not found");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

        List<Item> items = List.of(
                new Item("Labor", "Labor Work", 50, new BigDecimal("50.00")),
                new Item("Material", "Boxes", 50, new BigDecimal("50.00")),
                new Item("Other", "Misc", 50, new BigDecimal("50.00"))
        );

        BigDecimal subtotal = items.stream()
                .map(item -> item.amount() != null ? item.amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxRatePercent = new BigDecimal("5.00"); // 5%
        BigDecimal totalTax = subtotal.multiply(taxRatePercent).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.add(totalTax).subtract(discount);

        Map<String, Object> params = new HashMap<>();
        params.put("fromCompany", "Your Company Name");
        params.put("fromAddress", "Street Address, City");
        params.put("fromPhone", "Phone: 555-111-2222");
        params.put("fromEmail", "info@company.com");

        params.put("clientCompany", "Client Company Name");
        params.put("contactName", "Contact Name");
        params.put("clientAddress", "Client Street\nCity");
        params.put("clientPhone", "Client Phone");

        params.put("invoiceNo", "#" + invoiceId);
        params.put("invoiceDate", "12/25/2024");
        params.put("dueDate", "2025-10-01");

        params.put("subtotal", subtotal);
        params.put("discount", discount);
        params.put("taxRate", taxRatePercent);
        params.put("bankName", "Bank Name");
        params.put("accountNo", "Account No.");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        log.info("Generated invoice PDF for invoiceId={} (size={} bytes)", invoiceId, pdfBytes != null ? pdfBytes.length : 0);
        return pdfBytes;
    }

    public byte[] generateSampleInvoice() throws JRException {
        log.info("Request to generate sample invoice (no payload)");
        byte[] pdf = generateInvoice(
                "Sample Company",
                "123 Sample Street\nCity",
                "Sample Company Phone",
                "sample@company.com",
                "Sample Client",
                "456 Client Street\nCity",
                "555-123-4567",
                "INV-1001",
                "2024-10-01",
                "2024-10-15",
                "Sample Bank",
                "1234567890",
                List.of(
                        new Item("Labor", "Labor Work", 2, new BigDecimal("100.00")),
                        new Item("Material", "Material Box", 1, new BigDecimal("50.00"))
                )
        );
        log.info("Sample invoice generated (size={} bytes)", pdf != null ? pdf.length : 0);
        return pdf;
    }

    public byte[] generateInvoice(
            String fromCompany,
            String fromAddress,
            String fromPhone,
            String fromEmail,
            String clientName,
            String clientAddress,
            String clientPhone,
            String invoiceNo,
            String invoiceDate,
            String dueDate,
            String bankName,
            String accountNo,
            List<Item> items
    ) throws JRException {

        log.info("Request to generate invoice (invoiceNo={}) for client='{}'", invoiceNo, clientName);

        InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
        if (jrxml == null) {
            log.error("JRXML template not found at /reports/invoice.jrxml while generating invoiceNo={}", invoiceNo);
            throw new RuntimeException("invoice.jrxml not found");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

        if (items == null) {
            log.warn("Items list is null for invoiceNo={}, treating as empty list", invoiceNo);
            items = List.of();
        }
        log.debug("Calculating subtotal for invoiceNo={}, itemsCount={}", invoiceNo, items.size());

        BigDecimal subtotal = items.stream()
                .map(item -> item.amount() != null ? item.amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Subtotal for invoiceNo={} -> {}", invoiceNo, subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxRatePercent = new BigDecimal("5.00"); // 5%
        BigDecimal totalTax = subtotal.multiply(taxRatePercent).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.add(totalTax).subtract(discount);

        log.debug("Computed tax and total for invoiceNo={} : taxRate={}%, totalTax={}, total={}",
                invoiceNo, taxRatePercent, totalTax, total);

        Map<String, Object> params = new HashMap<>();
        params.put("fromCompany", fromCompany);
        params.put("fromAddress", fromAddress);
        params.put("fromPhone", fromPhone);
        params.put("fromEmail", fromEmail);

        params.put("clientCompany", clientName);
        params.put("contactName", clientName);
        params.put("clientAddress", clientAddress);
        params.put("clientPhone", clientPhone);

        params.put("invoiceNo", invoiceNo);
        params.put("invoiceDate", invoiceDate);
        params.put("dueDate", dueDate);

        params.put("subtotal", subtotal);
        params.put("discount", discount);
        params.put("taxRate", taxRatePercent);

        params.put("bankName", bankName);
        params.put("accountNo", accountNo);

        log.debug("Filling Jasper report for invoiceNo={}", invoiceNo);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

        byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
        log.info("Invoice PDF generated for invoiceNo={} (size={} bytes)", invoiceNo, pdf != null ? pdf.length : 0);
        return pdf;
    }

    /**
     * Public method invoked by scheduler to validate and process invoices.
     * Replace demo behaviour with your actual DB lookup / generation / email flow.
     */
    public void validateAndProcessInvoices(String trigger) {
        log.info("[INVOICE PROCESS] validateAndProcessInvoices triggered by: {}", trigger);

        try {
            // Demo / smoke-test: generate a sample invoice to prove scheduler end-to-end.
            byte[] pdf = generateSampleInvoice();
            log.info("[INVOICE PROCESS] Demo sample invoice generated by scheduler '{}', size={} bytes",
                    trigger, pdf != null ? pdf.length : 0);
        } catch (JRException e) {
            log.error("[INVOICE PROCESS] JRException in scheduler '{}': {}", trigger, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[INVOICE PROCESS] Unexpected error in validateAndProcessInvoices('{}'): {}", trigger, e.getMessage(), e);
        }
    }
}
