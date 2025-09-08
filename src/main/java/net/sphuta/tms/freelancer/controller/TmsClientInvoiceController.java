//package net.sphuta.tms.freelancer.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sphuta.tms.freelancer.constants.TmsMessages;
//import net.sphuta.tms.freelancer.dto.TmsInvoiceDto;
//import net.sphuta.tms.freelancer.service.impl.TmsInvoiceService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
///**
// * ==========================================================
// * TmsClientInvoiceController
// * ==========================================================
// *
// * REST controller for handling **Invoice operations**.
// *
// * Responsibilities:
// * - Create invoices from selected time entries.
// * - Send/issue invoices (transition from DRAFT → SENT).
// *
// * Observability:
// * - DEBUG logs for request entry.
// * - WARN logs for suspicious input (e.g., missing timeEntryIds).
// * - TRACE logs for fine-grained request details (ids count).
// * - INFO logs for successful operations.
// * - Execution time measured to track performance.
// */
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping(TmsMessages.INVOICE_BASE_PATH)
//@Tag(name = "Invoices")
//public class TmsClientInvoiceController {
//
//    /**
//     * Service layer handling invoice-related business logic.
//     */
//    @Autowired
//    private TmsInvoiceService service;
//
//    /**
//     * Create a new invoice from a list of approved time entries.
//     *
//     * <p>Behavior:</p>
//     * <ul>
//     *   <li>Logs clientId and count of timeEntryIds.</li>
//     *   <li>Warns if request payload contains no timeEntryIds.</li>
//     *   <li>Delegates invoice creation to {@link TmsInvoiceService}.</li>
//     *   <li>Returns HTTP 200 with the created invoice body.</li>
//     * </ul>
//     *
//     * @param req the invoice creation request
//     * @return the created invoice in the response body
//     */
//    @Operation(summary = "Create invoice from selected timeEntryIds")
//    @PostMapping
//    public ResponseEntity<TmsInvoiceDto> create(@Valid @RequestBody TmsInvoiceDto req) {
//
//        log.debug(TmsMessages.LOG_INVOICE_CREATE_REQUEST, req.clientId());
//        long _startNs = System.nanoTime();
//
//        // ---- sanity checks ----
//        if (req.timeEntryIds() == null || req.timeEntryIds().isEmpty()) {
//            log.warn(TmsMessages.LOG_INVOICE_NO_TIME_ENTRIES_WARN, req.clientId());
//        } else {
//            log.trace(TmsMessages.LOG_INVOICE_TIME_ENTRIES_TRACE, req.timeEntryIds().size());
//        }
//
//        // ---- delegate to service ----
//        TmsInvoiceDto body = service.createFromEntries(req);
//
//        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
//        log.info(TmsMessages.LOG_INVOICE_CREATED_SUCCESS, body.id(), body.clientId(), _tookMs);
//
//        return ResponseEntity.ok(body);
//    }
//
//    /**
//     * Transition an invoice from DRAFT → SENT.
//     *
//     * <p>Behavior:</p>
//     * <ul>
//     *   <li>Logs invoice id.</li>
//     *   <li>Delegates to service to perform status transition.</li>
//     *   <li>Returns 200 OK with updated invoice body.</li>
//     * </ul>
//     *
//     * @param invoiceId id of the invoice to send
//     * @return updated invoice with SENT status
//     */
//    @Operation(summary = "Send/Issue invoice (transition DRAFT → SENT)")
//    @PostMapping(TmsMessages.INVOICE_SEND_PATH)
//    public ResponseEntity<TmsInvoiceDto> send(@PathVariable Integer invoiceId) {
//
//        log.debug(TmsMessages.LOG_INVOICE_SEND_REQUEST, invoiceId);
//        long _startNs = System.nanoTime();
//
//        // ---- delegate to service ----
//        TmsInvoiceDto body = service.send(invoiceId);
//
//        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
//        log.info(TmsMessages.LOG_INVOICE_SENT_SUCCESS, invoiceId, body.status(), _tookMs);
//
//        return ResponseEntity.ok(body);
//    }
//}
