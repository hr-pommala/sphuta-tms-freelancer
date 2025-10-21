package net.sphuta.tms.freelancer.controller;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================
 *  ReportController
 * ==============================================================
 * This controller exposes REST endpoints for generating invoice PDF reports.
 * It acts as a thin routing layer that delegates all business logic,
 * report generation, and response creation to the {@link ReportService}.
 *
 * Responsibilities:
 *  - Accept client requests for invoice generation.
 *  - Log request activity for observability.
 *  - Delegate processing to service layer.
 *  - Return PDF responses with appropriate HTTP headers.
 *
 * Endpoints:
 *  - GET  /reports/invoice → Generates a sample invoice (no payload)
 *  - POST /reports/invoice → Generates a custom invoice (with JSON payload)
 * ==============================================================
 */
@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {

    /**
     * Injected ReportService dependency.
     * Handles all JasperReports-related logic.
     */
    @Autowired
    private ReportService reportService;

    /**
     * ==============================================================
     * 🧾 GET /reports/invoice
     * ==============================================================
     * Endpoint to generate a **sample invoice PDF**.
     *
     * This method:
     *  - Logs the incoming request.
     *  - Delegates full invoice generation and PDF preparation
     *    to {@link ReportService#buildSampleInvoiceResponse()}.
     *  - Returns a PDF as a downloadable HTTP response.
     *
     * @return ResponseEntity<byte[]> PDF file stream with HTTP headers
     * ==============================================================
     */
    @GetMapping(value = "/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> sampleInvoice() {
        // 🔹 Log request intent
        log.info(" Request received: Generate sample invoice (GET /reports/invoice)");

        // 🔹 Delegate business logic and response preparation to service layer
        return reportService.buildSampleInvoiceResponse();
    }

    /**
     * ==============================================================
     * 🧾 POST /reports/invoice
     * ==============================================================
     * Endpoint to generate a **custom invoice PDF** using client-provided data.
     *
     * This method:
     *  - Accepts JSON body as {@link InvoicePayload}.
     *  - Logs the request and its payload for traceability.
     *  - Delegates all PDF generation and header construction
     *    to {@link ReportService#buildInvoiceResponse(InvoicePayload)}.
     *  - Returns a generated invoice as an application/pdf download.
     *
     * Example Request:
     * <pre>
     * POST /reports/invoice
     * Content-Type: application/json
     * {
     *   "fromCompany": "Acme Solutions",
     *   "clientName": "John Doe",
     *   "invoiceNo": "INV-1001",
     *   ...
     * }
     * </pre>
     *
     * @param payload JSON payload containing invoice details
     * @return ResponseEntity<byte[]> PDF response (downloadable)
     * ==============================================================
     */
    @PostMapping(value = "/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> invoiceWithPayload(@RequestBody InvoicePayload payload) {
        // 🔹 Log request initiation
        log.info("Request received: Generate custom invoice (POST /reports/invoice)");

        // 🔹 Log request payload for debugging (contains company, client, invoice info)
        log.debug("Payload: {}", payload);

        // 🔹 Delegate entire processing to ReportService
        return reportService.buildInvoiceResponse(payload);
    }
}
