package net.sphuta.tms.controller;

import jakarta.validation.Valid;
import net.sphuta.tms.dto.*;
import net.sphuta.tms.response.ApiResponse;
import net.sphuta.tms.response.PageResponse;
import net.sphuta.tms.service.TimeEntryService;
import net.sphuta.tms.service.TimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * TmsController
 *
 * <p>REST controller for Timesheets and Time Entries.</p>
 *
 * <p>Demonstrates the usage of:</p>
 * <ul>
 *   <li>{@code @ModelAttribute} for query parameter binding (filters)</li>
 *   <li>{@code @RequestBody} for JSON payloads</li>
 *   <li>{@code @PathVariable} for path params</li>
 *   <li>HTTP coverage: GET / POST / PUT / DELETE / PATCH</li>
 * </ul>
 *
 * <p><b>Note:</b> This class adds structured logging statements without altering existing
 * functionality or behavior.</p>
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class TmsController {

    /** Class logger (SLF4J). */
    private static final Logger log = LoggerFactory.getLogger(TmsController.class);

    /** Timesheet operations service. */
    private final TimesheetService timesheetService;

    /** Time entry operations service. */
    private final TimeEntryService timeEntryService;

    /**
     * All-args constructor for dependency injection.
     *
     * @param t injected {@link TimesheetService}
     * @param e injected {@link TimeEntryService}
     */
    public TmsController(TimesheetService t, TimeEntryService e) {
        this.timesheetService = t;
        this.timeEntryService = e;
    }

    /**
     * List timesheets with optional filters (query params).
     *
     * @param filters {@link TimesheetDtos.TimesheetFilters} bound via {@code @ModelAttribute}
     * @return {@link ApiResponse} containing a {@link PageResponse} of {@link TimesheetDtos.TimesheetSummary}
     */
    @GetMapping("/timesheets")
    public ResponseEntity<ApiResponse<PageResponse<TimesheetDtos.TimesheetSummary>>> list(
            @Valid @ModelAttribute TimesheetDtos.TimesheetFilters filters) {

        // -- Log method entry with key filter values (safe toString on record) --
        log.info("GET /timesheets called with filters: {}", filters);

        // -- Delegate to service layer; no functional changes --
        var data = timesheetService.list(filters);

        // -- Log result meta (page info) for observability --
        log.debug("Timesheets page: number={}, size={}, totalElements={}, totalPages={}",
                data.page().number(), data.page().size(), data.page().totalElements(), data.page().totalPages());

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Create a new timesheet.
     *
     * @param req {@link TimesheetDtos.TimesheetCreateRequest} payload
     * @return created timesheet detail wrapped in {@link ApiResponse}
     */
    @PostMapping("/timesheets")
    public ResponseEntity<ApiResponse<TimesheetDtos.TimesheetDetail>> create(
            @Valid @RequestBody TimesheetDtos.TimesheetCreateRequest req) {

        // -- Log the intent with minimal payload details --
        log.info("POST /timesheets create request received for projectId={}, periodStart={}, periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        var data = timesheetService.create(req);

        // -- Log the resulting timesheet identifier and status --
        log.debug("Timesheet created: id={}, status={}", data.id(), data.status());

        return ResponseEntity.status(201).body(ApiResponse.ok(data));
    }

    /**
     * Retrieve timesheet details by ID.
     *
     * @param id timesheet identifier (UUID)
     * @return timesheet detail in {@link ApiResponse}
     */
    @GetMapping("/timesheets/{id}")
    public ResponseEntity<ApiResponse<TimesheetDtos.TimesheetDetail>> get(@PathVariable UUID id) {

        // -- Log path parameter for traceability --
        log.info("GET /timesheets/{} called", id);

        var data = timesheetService.get(id);

        // -- Log minimal information about the returned resource --
        log.debug("Fetched timesheet: id={}, status={}, entries={}",
                data.id(), data.status(), data.entries() == null ? 0 : data.entries().size());

        return ResponseEntity.ok(ApiResponse.ok(timesheetService.get(id)));
    }

    /**
     * Bulk upsert entries for a timesheet.
     *
     * @param timesheetId path param ID for the timesheet
     * @param req         bulk upsert payload
     * @return bulk operation summary wrapped in {@link ApiResponse}
     */
    @PutMapping("/timesheets/{id}/entries")
    public ResponseEntity<ApiResponse<TimeEntryDtos.BulkUpsertResponse>> bulkUpsert(
            @PathVariable("id") UUID timesheetId,
            @Valid @RequestBody TimeEntryDtos.BulkUpsertRequest req) {

        // -- Log request intent and light-weight stats (entries count) --
        log.info("PUT /timesheets/{}/entries bulk upsert called (rows={})",
                timesheetId, (req.entries() == null ? 0 : req.entries().size()));

        var data = timesheetService.bulkUpsert(timesheetId, req);

        // -- Log result counters to assist troubleshooting --
        log.debug("Bulk upsert result for timesheet {}: inserted={}, updated={}, deleted={}, totalHours={}",
                timesheetId, data.inserted(), data.updated(), data.deleted(), data.totalHours());

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Submit (approve) a timesheet. Idempotent operation.
     *
     * @param id timesheet ID
     * @return updated timesheet detail wrapped in {@link ApiResponse}
     */
    @PostMapping("/timesheets/{id}/submit")
    public ResponseEntity<ApiResponse<TimesheetDtos.TimesheetDetail>> submit(@PathVariable UUID id) {

        // -- Log the action with the resource id --
        log.info("POST /timesheets/{}/submit called", id);

        var data = timesheetService.submit(id);

        // -- Log status after submission --
        log.debug("Timesheet {} submitted; new status={}", id, data.status());

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Lock a timesheet (prevents further edits).
     *
     * @param id timesheet ID
     * @return confirmation message in {@link ApiResponse}
     */
    @PatchMapping("/timesheets/{id}/lock")
    public ResponseEntity<ApiResponse<String>> lock(@PathVariable UUID id) {

        // -- Log the action (no functional change) --
        log.info("PATCH /timesheets/{}/lock called", id);

        timesheetService.lock(id);

        // -- Log completion --
        log.debug("Timesheet {} locked", id);

        return ResponseEntity.ok(ApiResponse.message("Timesheet locked"));
    }

    /**
     * Create a single time entry.
     *
     * @param req entry creation payload
     * @return created entry wrapped in {@link ApiResponse}
     */
    @PostMapping("/time-entries")
    public ResponseEntity<ApiResponse<TimeEntryDtos.TimeEntryResponse>> createEntry(
            @Valid @RequestBody TimeEntryDtos.TimeEntryCreateRequest req) {

        // -- Log minimal intent with target timesheet and date --
        log.info("POST /time-entries create request for timesheetId={}, entryDate={}, hours={}",
                req.timesheetId(), req.entryDate(), req.hours());

        var data = timeEntryService.create(req);

        // -- Log resulting entry id for correlation --
        log.debug("Time entry created: id={}, timesheetId={}, hours={}", data.id(), data.timesheetId(), data.hours());

        return ResponseEntity.status(201).body(ApiResponse.ok(data));
    }

    /**
     * Delete a single time entry by ID.
     *
     * @param entryId time entry ID
     * @return no content (204) on success
     */
    @DeleteMapping("/time-entries/{entryId}")
    public ResponseEntity<ApiResponse<String>> deleteEntry(@PathVariable("entryId") UUID entryId) {

        // -- Log the delete intent with identifier --
        log.info("DELETE /time-entries/{} called", entryId);

        timeEntryService.delete(entryId);

        // -- Log successful deletion; response remains 204 No Content --
        log.debug("Time entry {} deleted", entryId);

        return ResponseEntity.noContent().build();
    }
}
