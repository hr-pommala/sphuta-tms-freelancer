package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.service.impl.TimeEntryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * <h2>TimeEntryController</h2>
 *
 * REST endpoints for interacting with **time entries**.
 * <p>
 * Currently exposes a single read endpoint to fetch <em>approved</em> and
 * <em>uninvoiced</em> time entries for a given client within a date range.
 * </p>
 *
 * <p><b>Logging policy</b>:
 * <ul>
 *   <li><b>DEBUG</b> — log inbound request parameters (clientId/from/to)</li>
 *   <li><b>WARN</b> — log suspicious but non-fatal input (e.g., from &gt; to)</li>
 *   <li>Intentionally minimal to avoid duplicate service calls or changing behavior</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/time-entries")
@Tag(name = "Time Entries")
public class TimeEntryController {

    /** Business service used to query time-entry data. */
    private final TimeEntryService service;

    /**
     * Fetch approved, uninvoiced time entries for a client in a date range.
     *
     * @param clientId Client UUID (must correspond to an existing client)
     * @param from     Start of the date range (inclusive), ISO format (yyyy-MM-dd)
     * @param to       End of the date range (inclusive), ISO format (yyyy-MM-dd)
     * @return {@link TmsDto.UninvoicedResponse} containing matching entries
     */
    @Operation(summary = "Fetch approved, uninvoiced time entries for a client in a date range")
    @GetMapping("/uninvoiced")
    public ResponseEntity<TmsDto.UninvoicedResponse> uninvoiced(
            @Parameter(description = "Client UUID")
            @RequestParam UUID clientId,

            @Parameter(description = "From date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "To date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // ─────────────────────────────────────────────────────────────────────────────
        // Request logging — do not change behavior, just trace inputs.
        // ─────────────────────────────────────────────────────────────────────────────
        log.debug("HTTP GET /time-entries/uninvoiced clientId={}, from={}, to={}", clientId, from, to);

        // ─────────────────────────────────────────────────────────────────────────────
        // Soft validation log (non-blocking): if 'from' is after 'to', it's suspicious.
        // We only WARN here to keep the method behavior unchanged as requested.
        // ─────────────────────────────────────────────────────────────────────────────
        if (from != null && to != null && from.isAfter(to)) {
            log.warn("Suspicious date range: 'from' ({}) is after 'to' ({}) for client {}", from, to, clientId);
        }

        // Delegate directly to service without altering the existing return pattern.
        return ResponseEntity.ok(service.findUninvoiced(clientId, from, to));
    }
}
