package net.sphuta.tms.freelancer.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.BulkUpsertDto;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.dto.TmsTimesheetDto;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.TmsTimeEntryService;
import net.sphuta.tms.freelancer.service.TmsTimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

/**
 * REST controller for managing Timesheets and Time Entries.
 * <p>
 * Provides endpoints to:
 * <ul>
 *     <li>Create and retrieve timesheets</li>
 *     <li>Bulk upsert (insert/update/delete) time entries</li>
 *     <li>Submit and lock timesheets</li>
 *     <li>Create and delete time entries</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Validated
public class TmsTimesheetController {

    /** Service for handling timesheet operations */
    @Autowired
    private TmsTimesheetService tmsTimesheetService;

    /** Service for handling time entry operations */
    @Autowired
    private TmsTimeEntryService tmsTimeEntryService;

    /**
     * Create a new timesheet for a project.
     *
     * @param req request payload containing projectId, periodStart, and periodEnd
     * @return created timesheet wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Create Timesheet", description = "Create a new timesheet for a project with given period.")
    @PostMapping("/timesheets")
    public ResponseEntity<TmsApiResponse<TmsTimesheetDto>> create(
            @Valid @RequestBody TmsTimesheetDto req) {
        // Log request
        log.info("POST /timesheets projectId={} periodStart={} periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        // Call service layer
        var data = tmsTimesheetService.create(req);

        // Log result
        log.debug("Timesheet created id={} status={}", data.id(), data.status());

        // Return response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TmsApiResponse.success(HttpStatus.CREATED, TmsMessages.TIMESHEET_CREATED, data));
    }

    /**
     * Fetch details of a specific timesheet.
     *
     * @param id timesheet ID
     * @return detailed timesheet DTO wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Get Timesheet", description = "Retrieve details of a specific timesheet by ID.")
    @GetMapping("/timesheets/{id}")
    public ResponseEntity<TmsApiResponse<TmsTimesheetDto>> get(@PathVariable Integer id) {
        log.info("GET /timesheets/{}", id);

        var data = tmsTimesheetService.get(id);

        // If entries are null, log size as 0
        log.debug("Fetched timesheet id={} status={} entries={}",
                data.id(), data.status(), data.entries() == null ? 0 : data.entries().size());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.TIMESHEET_FETCHED, data));
    }

    /**
     * Bulk upsert (insert, update, delete) time entries for a timesheet.
     *
     * @param timesheetId ID of the timesheet
     * @param req bulk upsert request containing entries and operation mode
     * @return summary of bulk upsert operation wrapped in {@link BulkUpsertDto}
     */
    @Operation(summary = "Bulk Upsert Entries", description = "Insert, update, or delete multiple entries in a timesheet.")
    @PutMapping("/timesheets/{id}/entries")
    public ResponseEntity<TmsApiResponse<BulkUpsertDto>> bulkUpsert(
            @PathVariable("id") Integer timesheetId,
            @Valid @RequestBody BulkUpsertDto req) {

        log.info("PUT /timesheets/{}/entries rows={}",
                timesheetId, (req.entries() == null ? 0 : req.entries().size()));

        var data = tmsTimesheetService.bulkUpsert(timesheetId, req);

        log.debug("Bulk upsert timesheet {} -> inserted={} updated={} deleted={} totalHours={}",
                timesheetId, data.inserted(), data.updated(), data.deleted(), data.totalHours());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.BULK_UPSERT_COMPLETED, data));
    }

    /**
     * Submit a timesheet for approval.
     *
     * @param id timesheet ID
     * @return updated timesheet status after submission
     */
    @Operation(summary = "Submit Timesheet", description = "Submit a timesheet for review and approval.")
    @PostMapping("/timesheets/{id}/submit")
    public ResponseEntity<TmsApiResponse<TmsTimesheetDto>> submit(@PathVariable Integer id) {
        log.info("POST /timesheets/{}/submit", id);

        var data = tmsTimesheetService.submit(id);

        log.debug("Timesheet {} submitted; status={}", id, data.status());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.TIMESHEET_SUBMITTED, data));
    }

    /**
     * Lock a timesheet to prevent further modifications.
     *
     * @param id timesheet ID
     * @return success message
     */
    @Operation(summary = "Lock Timesheet", description = "Lock a timesheet to prevent further modifications.")
    @PatchMapping("/timesheets/{id}/lock")
    public ResponseEntity<TmsApiResponse<String>> lock(@PathVariable Integer id) {
        log.info("PATCH /timesheets/{}/lock", id);

        tmsTimesheetService.lock(id);

        log.debug("Timesheet {} locked", id);

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.TIMESHEET_LOCKED, "Timesheet locked"));
    }

    /**
     * Create a new time entry under an existing timesheet.
     *
     * @param req request payload containing timesheetId, entryDate, hours, etc.
     * @return created time entry
     */
    @Operation(summary = "Create Time Entry", description = "Create a new time entry under an existing timesheet.")
    @PostMapping("/time-entries")
    public ResponseEntity<TmsApiResponse<TimeEntryDto>> createEntry(
            @Valid @RequestBody TimeEntryDto req) {

        log.info("POST /time-entries timesheetId={} entryDate={} hours={}",
                req.timesheetId(), req.entryDate(), req.hours());

        var data = tmsTimeEntryService.create(req);

        log.debug("Time entry created id={} timesheetId={} hours={}", data.id(), data.timesheetId(), data.hours());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TmsApiResponse.success(HttpStatus.CREATED, TmsMessages.TIME_ENTRY_CREATED, data));
    }

    /**
     * Delete a specific time entry by ID.
     *
     * @param entryId ID of the time entry
     * @return success response
     */
    @Operation(summary = "Delete Time Entry", description = "Delete a specific time entry by its ID.")
    @DeleteMapping("/time-entries/{entryId}")
    public ResponseEntity<TmsApiResponse<Void>> deleteEntry(@PathVariable("entryId") Integer entryId) {
        log.info("DELETE /time-entries/{}", entryId);

        tmsTimeEntryService.delete(entryId);

        log.debug("Time entry {} deleted", entryId);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.TIME_ENTRY_DELETED)
        );
    }
}
