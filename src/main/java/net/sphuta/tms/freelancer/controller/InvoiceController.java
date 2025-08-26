package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.service.impl.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * <h2>InvoiceController</h2>
 *
 * REST endpoints for basic invoice operations:
 * <ul>
 *   <li>Create an invoice from selected, approved, uninvoiced time entries.</li>
 *   <li>Send/issue an existing invoice (DRAFT → SENT).</li>
 * </ul>
 *
 * <p><b>Logging policy</b>:
 * <br/>- DEBUG: request entry logs (paths/ids) and DTO build trace
 * <br/>- INFO: successful operations (created/sent)
 * <br/>- WARN: suspicious inputs (e.g., empty timeEntryIds)
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices")
public class InvoiceController {

    /** Business service handling invoice creation/sending. */
    private final InvoiceService service;

    /**
     * Create an invoice from selected time entry ids.
     * <p>
     * The service layer enforces:
     * <ul>
     *   <li>All time entries exist</li>
     *   <li>All belong to the same client as {@code clientId}</li>
     *   <li>All are APPROVED and not yet invoiced</li>
     * </ul>
     * </p>
     *
     * @param req validated request body containing client, dates, currency and entry ids
     * @return invoice read model
     */
    @Operation(summary = "Create invoice from selected timeEntryIds")
    @PostMapping
    public ResponseEntity<TmsDto.InvoiceResponse> create(@Valid @RequestBody TmsDto.InvoiceCreateRequest req) {
        // ─────────────────────────────────────────────────────────────────────────────
        // Request logging
        // ─────────────────────────────────────────────────────────────────────────────
        log.debug("HTTP POST /invoices for client {}", req.clientId());
        if (req.timeEntryIds() == null || req.timeEntryIds().isEmpty()) {
            // Not a failure—validation/business rules run in service. This is an early hint.
            log.warn("Invoice create request received with no timeEntryIds for client {}", req.clientId());
        } else {
            log.trace("Invoice create request contains {} timeEntryIds", req.timeEntryIds().size());
        }

        // Delegate to service layer
        Invoice inv = service.createFromEntries(req);

        // Build and return response DTO
        TmsDto.InvoiceResponse body = toDto(inv);

        // Success log for auditability
        log.info("Invoice {} created for client {} (status={})",
                inv.getId(), inv.getClientId(), inv.getStatus());

        return ResponseEntity.ok(body);
    }

    /**
     * Send/issue an invoice. Only invoices in DRAFT state can be sent.
     *
     * @param invoiceId invoice identifier
     * @return updated invoice read model with new status
     */
    @Operation(summary = "Send/Issue invoice (transition DRAFT → SENT)")
    @PostMapping("/{invoiceId}/send")
    public ResponseEntity<TmsDto.InvoiceResponse> send(@PathVariable UUID invoiceId) {
        // ─────────────────────────────────────────────────────────────────────────────
        // Request logging
        // ─────────────────────────────────────────────────────────────────────────────
        log.debug("HTTP POST /invoices/{}/send", invoiceId);

        // Delegate to service layer
        Invoice inv = service.send(invoiceId);

        // Build and return response DTO
        TmsDto.InvoiceResponse body = toDto(inv);

        // Success log for audit trail
        log.info("Invoice {} sent (status={})", invoiceId, inv.getStatus());

        return ResponseEntity.ok(body);
    }

    /**
     * Map entity to response DTO.
     * Kept as a private helper to avoid leaking entity internals.
     *
     * @param i the invoice entity
     * @return response record for API consumers
     */
    private TmsDto.InvoiceResponse toDto(Invoice i) {
        // Trace-level is useful when debugging serialization/mapping issues.
        log.trace("Building InvoiceResponse DTO for invoice {}", i.getId());
        return TmsDto.InvoiceResponse.builder()
                .id(i.getId()).clientId(i.getClientId())
                .issueDate(i.getIssueDate()).dueDate(i.getDueDate())
                .currencyCode(i.getCurrencyCode()).status(i.getStatus().name())
                .notes(i.getNotes()).createdAt(i.getCreatedAt()).updatedAt(i.getUpdatedAt()).build();
    }
}
