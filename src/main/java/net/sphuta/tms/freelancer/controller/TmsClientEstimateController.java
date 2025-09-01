package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.TmsEstimateDto;
import net.sphuta.tms.freelancer.service.impl.TmsEstimateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ==========================================================
 * TmsClientEstimateController
 * ==========================================================
 *
 * REST controller exposing endpoints for managing estimates.
 *
 * Responsibilities:
 * - Accept client requests to create estimates with line items.
 * - Validate payloads and log useful diagnostic information.
 * - Delegate to {@link TmsEstimateService} for business logic.
 *
 * Observability:
 * - DEBUG logs for entry and input parameters.
 * - WARN logs if the request is suspicious (e.g., no items provided).
 * - TRACE logs for finer details (item counts).
 * - INFO logs for successful creation events.
 * - Execution time measured for performance awareness.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(TmsMessages.ESTIMATE_BASE_PATH)
@Tag(name = "Estimates")
public class TmsClientEstimateController {

    /**
     * Service layer dependency that handles estimate creation and persistence.
     */
    @Autowired
    private TmsEstimateService service;

    /**
     * Create a new estimate for a given client with associated line items.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Logs clientId and item count.</li>
     *   <li>Delegates to service for creation logic.</li>
     *   <li>Returns HTTP 200 with the created estimate in the response body.</li>
     * </ul>
     *
     * <p>Validation:</p>
     * <ul>
     *   <li>Rejects invalid payloads via {@link Valid} annotations on DTO.</li>
     *   <li>Warns if no items are included (though still delegates).</li>
     * </ul>
     */
    @Operation(summary = "Create an estimate with items")
    @PostMapping
    public ResponseEntity<TmsEstimateDto> create(@Valid @RequestBody TmsEstimateDto req) {

        log.debug(TmsMessages.LOG_ESTIMATE_CREATE_REQUEST, req.clientId());
        long _startNs = System.nanoTime();

        // ---- request sanity checks ----
        if (req.items() == null || req.items().isEmpty()) {
            log.warn(TmsMessages.LOG_ESTIMATE_NO_ITEMS_WARN, req.clientId());
        } else {
            log.trace(TmsMessages.LOG_ESTIMATE_ITEMS_TRACE, req.items().size());
        }

        // ---- delegate to service ----
        TmsEstimateDto body = service.create(req);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.info(TmsMessages.LOG_ESTIMATE_CREATED_SUCCESS, body.id(), body.clientId(), _tookMs);

        return ResponseEntity.ok(body);
    }
}
