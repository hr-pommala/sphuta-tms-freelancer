package net.sphuta.tms.freelancer.controller;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * =============================================================
 * ReportController
 * =============================================================
 * This controller handles all report-related API endpoints,
 * primarily focusing on generating PDF invoices for freelancers.
 *
 * Features:
 * - Accepts invoice details (via InvoicePayload DTO)
 * - Delegates PDF generation logic to ReportService
 * - Returns the generated invoice as a PDF response
 *
 * Endpoint base path: /reports
 */
@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {

    /**
     * ReportService is responsible for the core business logic
     * related to generating reports, such as invoices.
     * It is automatically injected by Spring using @Autowired.
     */
    @Autowired
    private ReportService reportService;

    /**
     * =============================================================
     * Method: invoiceWithPayload
     * =============================================================
     * Purpose:
     *  - Accepts custom invoice details in JSON format.
     *  - Generates a PDF invoice dynamically based on the given data.
     *
     * HTTP Method: POST
     * Endpoint: /reports/invoice
     * Consumes: application/json (InvoicePayload)
     * Produces: application/pdf (Generated Invoice)
     *
     * @param payload - InvoicePayload containing all invoice details
     * @return ResponseEntity containing the generated PDF as a byte stream
     */
    @PostMapping(value = "/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> invoiceWithPayload(@RequestBody InvoicePayload payload) {
        log.info("Request received: Generate custom invoice (POST /reports/invoice)");
        log.debug("Payload: {}", payload);
        // Delegate the actual PDF generation to ReportService
        // The service will handle business logic, PDF creation, and ResponseEntity preparation.
        return reportService.generateInvoicePdf(payload);
    }

    /**
     * Manual trigger endpoint for testing scheduler flows.
     * Usage: POST /reports/invoice/trigger/TEST
     */
    @PostMapping("/invoice/trigger/{trigger}")
    public ResponseEntity<?> triggerScheduler(@PathVariable String trigger) {
        log.info("Manual trigger invoked: {}", trigger);
        reportService.validateAndProcessInvoices(trigger);
        return ResponseEntity.ok(Map.of("status", "triggered", "trigger", trigger));
    }
}
