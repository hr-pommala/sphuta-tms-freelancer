package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.Estimate;
import net.sphuta.tms.freelancer.service.impl.EstimateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <h2>EstimateController</h2>
 *
 * REST controller that exposes endpoints for working with Estimates.
 * <ul>
 *   <li>Creates an estimate with one or more items.</li>
 *   <li>Uses DTO records for request/response, service layer for persistence.</li>
 *   <li>Swagger annotations are present for API documentation.</li>
 * </ul>
 *
 * <p><b>Logging</b>
 * <br/>- DEBUG: incoming request and key request values
 * <br/>- WARN: suspicious but non-fatal conditions (e.g., no items in request)
 * <br/>- INFO: successful creation summary
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/estimates")
@Tag(name = "Estimates")
public class EstimateController {

    /** Business service that handles estimate creation. */
    private final EstimateService service;

    /**
     * Create an estimate and return its representation.
     *
     * @param req validated request body (DTO record) containing header fields and line items
     * @return the created estimate as a response DTO
     */
    @Operation(summary = "Create an estimate with items")
    @PostMapping
    public ResponseEntity<TmsDto.EstimateResponse> create(@Valid @RequestBody TmsDto.EstimateCreateRequest req) {
        // ─────────────────────────────────────────────────────────────────────────────
        // Request logging (inputs only; no business logic changes)
        // ─────────────────────────────────────────────────────────────────────────────
        log.debug("HTTP POST /estimates for client {}", req.clientId());
        if (req.items() == null || req.items().isEmpty()) {
            // Not failing the request here—just surfacing a potentially suspicious input.
            log.warn("Estimate create request has no items for client {}", req.clientId());
        } else {
            log.trace("Estimate create request contains {} item(s)", req.items().size());
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // Delegate to service layer for creation
        // ─────────────────────────────────────────────────────────────────────────────
        Estimate e = service.create(req);

        // ─────────────────────────────────────────────────────────────────────────────
        // Build response DTO (no transformation logic changed)
        // ─────────────────────────────────────────────────────────────────────────────
        TmsDto.EstimateResponse body = TmsDto.EstimateResponse.builder()
                .id(e.getId()).clientId(e.getClientId())
                .issueDate(e.getIssueDate()).validUntil(e.getValidUntil())
                .currencyCode(e.getCurrencyCode()).notes(e.getNotes())
                .items(req.items()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();

        // ─────────────────────────────────────────────────────────────────────────────
        // Success logging (ID and client for traceability)
        // ─────────────────────────────────────────────────────────────────────────────
        log.info("Estimate {} created for client {} with {} item(s)",
                e.getId(), e.getClientId(), (req.items() == null ? 0 : req.items().size()));

        return ResponseEntity.ok(body);
    }
}
