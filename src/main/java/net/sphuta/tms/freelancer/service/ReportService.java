package net.sphuta.tms.freelancer.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.dto.Item;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================
 * 📄 ReportService
 * ==============================================================
 * Central service for creating invoice PDF reports using JasperReports.
 *
 * Responsibilities:
 *  - Compile JRXML templates located on the classpath (e.g. /reports/invoice.jrxml)
 *  - Prepare report parameter map (company, client, invoice metadata, totals)
 *  - Calculate subtotal, tax and grand totals in a null-safe manner
 *  - Fill Jasper reports and export to PDF bytes
 *  - Provide endpoint helper methods that build full {@link ResponseEntity<byte[]>}
 *    responses (headers + PDF content + error mapping) so controllers remain thin.
 *
 * Notes:
 *  - Methods that compile/fill/export Jasper reports throw {@link JRException}.
 *  - JRXML path must be available in classpath: src/main/resources/reports/invoice.jrxml
 * ==============================================================
 */
@Slf4j
@Service
public class ReportService {

    // -----------------------
    // Endpoint helper methods
    // -----------------------

    /**
     * Build the HTTP response for the "sample invoice" endpoint.
     *
     * This helper:
     *  - Delegates PDF generation to {@link #generateSampleInvoice()}
     *  - Adds appropriate HTTP headers for PDF download (Content-Type, Content-Disposition)
     *  - Catches {@link JRException} and generic {@link Exception} and maps them to 500 responses
     *
     * @return a {@link ResponseEntity} containing PDF bytes and headers on success,
     *         or a 500 INTERNAL_SERVER_ERROR response on failure.
     */
    public ResponseEntity<byte[]> buildSampleInvoiceResponse() {
        try {
            // Generate PDF bytes (may throw JRException)
            byte[] pdf = generateSampleInvoice();
            log.debug("Sample invoice generated successfully. Size: {} bytes", (pdf != null ? pdf.length : 0));

            // Prepare response headers to trigger file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("invoice.pdf")
                            .build()
            );

            log.info(" Sending generated sample invoice as response...");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            // Jasper-specific failure (template compile/fill/export)
            log.error(" JasperReports exception while generating sample invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // Any other unexpected runtime failure
            log.error(" Unexpected error generating sample invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Build the HTTP response for the "custom invoice" endpoint.
     *
     * This helper:
     *  - Extracts payload fields and delegates PDF generation to {@link #generateInvoice}
     *  - Adds HTTP headers for PDF download and handles exceptions similarly to the sample helper
     *
     * @param payload the invoice payload provided by the client (mapped from JSON)
     * @return a {@link ResponseEntity} containing PDF bytes and headers on success,
     *         or a 500 INTERNAL_SERVER_ERROR response on failure.
     */
    public ResponseEntity<byte[]> buildInvoiceResponse(InvoicePayload payload) {
        try {
            // Delegate PDF creation using payload values
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

            log.debug(" Custom invoice generated successfully. Size: {} bytes", (pdf != null ? pdf.length : 0));

            // Prepare response headers for downloadable PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("invoice_custom.pdf")
                            .build()
            );

            log.info("Sending generated custom invoice as response...");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (JRException e) {
            log.error(" JasperReports exception while generating custom invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error(" Unexpected error generating custom invoice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -----------------------
    // Existing generation methods (core business logic)
    // -----------------------

    /**
     * Generate an invoice PDF for a given invoice id using sample/demo data.
     *
     * This method demonstrates compiling a JRXML template, building a sample
     * item list, computing totals and exporting a JasperPrint to PDF bytes.
     *
     * @param invoiceId identifier used in invoice number and logging
     * @return PDF content as byte[]
     * @throws JRException if Jasper report compilation/fill/export fails
     */
    public byte[] generateInvoicePdf(Integer invoiceId) throws JRException {
        log.info("Request to generate invoice PDF for invoiceId={}", invoiceId);

        // Load JRXML template from classpath. Fail fast if missing.
        InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
        if (jrxml == null) {
            log.error("JRXML template not found at /reports/invoice.jrxml");
            throw new RuntimeException("invoice.jrxml not found");
        }

        // Compile the JRXML template into a JasperReport instance
        log.debug("Compiling JRXML template for invoiceId={}", invoiceId);
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

        // --- Demo sample items (replace with real data source when available) ---
        List<Item> items = List.of(
                new Item("Labor", "Labor Work", 50, new BigDecimal("50.00")),
                new Item("Material", "Boxes", 50, new BigDecimal("50.00")),
                new Item("Other", "Misc", 50, new BigDecimal("50.00"))
        );
        log.debug("Sample items prepared (count={}) for invoiceId={}", items.size(), invoiceId);

        // Null-safe subtotal: treat null amount as BigDecimal.ZERO
        BigDecimal subtotal = items.stream()
                .map(item -> item.amount() != null ? item.amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Calculated subtotal={} for invoiceId={}", subtotal, invoiceId);

        // Basic financial math (discount, tax calculation, total)
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxRatePercent = new BigDecimal("5.00"); // 5%
        BigDecimal totalTax = subtotal.multiply(taxRatePercent).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.add(totalTax).subtract(discount);

        log.debug("Computed totals for invoiceId={} : taxRate={}%, totalTax={}, discount={}, total={}",
                invoiceId, taxRatePercent, totalTax, discount, total);

        // Parameters map passed to Jasper template as $P{...}
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

        // Fill the report. JREmptyDataSource is used because this template
        // currently relies on parameters rather than a collection datasource.
        log.debug("Filling report for invoiceId={}", invoiceId);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

        // Export JasperPrint to PDF bytes and return
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        log.info("Generated invoice PDF for invoiceId={} (size={} bytes)", invoiceId, pdfBytes != null ? pdfBytes.length : 0);
        return pdfBytes;
    }

    /**
     * Generate a sample invoice PDF (no external payload).
     *
     * This convenience method delegates to {@link #generateSampleInvoice()}
     * using small sample data for quick testing or demo purposes.
     *
     * @return PDF content as byte[]
     * @throws JRException if report generation fails
     */
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

    /**
     * Generate an invoice PDF using provided payload parameters.
     *
     * Core business logic:
     *  - Validate template presence
     *  - Compile JRXML
     *  - Null-safely compute subtotal from items list
     *  - Compute tax and total
     *  - Prepare parameter map for the JRXML and fill the report
     *
     * Note: If you plan to render dynamic item rows in the report, replace
     * the JREmptyDataSource with a JRBeanCollectionDataSource(items) and add
     * <field> definitions to the JRXML.
     *
     * @param fromCompany issuer name
     * @param fromAddress issuer address
     * @param fromPhone issuer phone
     * @param fromEmail issuer email
     * @param clientName client / billed-to name
     * @param clientAddress client address
     * @param clientPhone client phone
     * @param invoiceNo invoice number string
     * @param invoiceDate invoice date string
     * @param dueDate due date string
     * @param items list of {@link Item} - may contain null amounts (handled)
     * @return PDF content as byte[]
     * @throws JRException if report compilation/fill/export fails
     */
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

        // Ensure JRXML template is present on the classpath
        InputStream jrxml = getClass().getResourceAsStream("/reports/invoice.jrxml");
        if (jrxml == null) {
            log.error("JRXML template not found at /reports/invoice.jrxml while generating invoiceNo={}", invoiceNo);
            throw new RuntimeException("invoice.jrxml not found");
        }

        // Compile template to JasperReport
        log.debug("Compiling JRXML template for invoiceNo={}", invoiceNo);
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

        // Defensive: if items list is null, treat as empty list to avoid NPEs
        if (items == null) {
            log.warn("Items list is null for invoiceNo={}, treating as empty list", invoiceNo);
            items = List.of();
        }
        log.debug("Calculating subtotal for invoiceNo={}, itemsCount={}", invoiceNo, items.size());

        // Null-safe subtotal calculation
        BigDecimal subtotal = items.stream()
                .map(item -> item.amount() != null ? item.amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Subtotal for invoiceNo={} -> {}", invoiceNo, subtotal);

        // Compute discount, tax and grand total
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxRatePercent = new BigDecimal("5.00"); // 5%
        BigDecimal totalTax = subtotal.multiply(taxRatePercent).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.add(totalTax).subtract(discount);

        log.debug("Computed tax and total for invoiceNo={} : taxRate={}%, totalTax={}, total={}",
                invoiceNo, taxRatePercent, totalTax, total);

        // Assemble parameters passed into the JRXML template ($P{...})
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

        // Optional bank/account info (empty by default)
        params.put("bankName", bankName);
        params.put("accountNo", accountNo);

        // Fill the report. If your template expects a collection of items,
        // switch to new JRBeanCollectionDataSource(items) and add fields in JRXML.
        log.debug("Filling Jasper report for invoiceNo={}", invoiceNo);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

        // Export to PDF and return bytes
        byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
        log.info("Invoice PDF generated for invoiceNo={} (size={} bytes)", invoiceNo, pdf != null ? pdf.length : 0);
        return pdf;
    }
}
