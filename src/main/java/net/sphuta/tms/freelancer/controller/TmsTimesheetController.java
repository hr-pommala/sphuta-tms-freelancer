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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Timesheets and Time Entries.
 *
 * <p>Exposes endpoints for:
 * <ul>
 *   <li>Creating, fetching, submitting and deleting timesheets</li>
 *   <li>Bulk upsert of time entries</li>
 *   <li>Creating and deleting individual time entries</li>
 *   <li>Weekly / monthly aggregated views per user and per-project day-by-day</li>
 * </ul>
 *
 * <p>Responses are wrapped in {@link TmsApiResponse} to provide uniform success/error structure.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Validated
public class TmsTimesheetController {

    /**
     * Timesheet service for timesheet-level operations.
     * Injected via constructor (Lombok @RequiredArgsConstructor).
     */
    @Autowired
    private TmsTimesheetService tmsTimesheetService;

    /**
     * Time-entry service for entry-level operations.
     * Injected via constructor (Lombok @RequiredArgsConstructor).
     */
    @Autowired
    private TmsTimeEntryService tmsTimeEntryService;

    // ----------------------------
    // Timesheet endpoints
    // ----------------------------

    /**
     * Create a new timesheet for a project.
     *
     * @param req request payload (projectId, periodStart, periodEnd)
     * @return created timesheet DTO wrapped in TmsApiResponse
     */
    @Operation(summary = "Create Timesheet", description = "Create a new timesheet for a project with a given period.")
    @PostMapping("/timesheets")
    public TmsApiResponse<TmsTimesheetDto> create(
            @Valid @RequestBody TmsTimesheetDto req) {

        log.info("POST /api/v1/timesheets projectId={} periodStart={} periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        var data = tmsTimesheetService.create(req);

        log.debug("Created timesheet id={} status={}", data.timesheetId(), data.status());
        return TmsApiResponse.created(TmsMessages.TIMESHEET_CREATED, data);
    }

    /**
     * Fetch details for a specific timesheet (includes entries and totals).
     *
     * @param id timesheet id
     * @return detailed timesheet DTO wrapped in TmsApiResponse
     */
    @Operation(summary = "Get Timesheet", description = "Retrieve details of a specific timesheet by ID.")
    @GetMapping("/timesheets/{id}")
    public TmsApiResponse<TmsTimesheetDto> get(@PathVariable int id) {
        log.info("GET /api/v1/timesheets/{}", id);

        var data = tmsTimesheetService.get(id);

        log.debug("Fetched timesheet id={} entriesCount={}", data.timesheetId(), data.entries() == null ? 0 : data.entries().size());
        return TmsApiResponse.success(TmsMessages.TIMESHEET_FETCHED, data);
    }

    /**
     * Submit a timesheet (mark as APPROVED / submit for review).
     *
     * @param id timesheet id
     * @return updated timesheet DTO wrapped in TmsApiResponse
     */
    @Operation(summary = "Submit Timesheet", description = "Submit a timesheet for review and approval.")
    @PostMapping("/timesheets/{id}/submit")
    public TmsApiResponse<TmsTimesheetDto> submit(@PathVariable int id) {
        log.info("POST /api/v1/timesheets/{}/submit", id);

        var data = tmsTimesheetService.submit(id);

        log.debug("Timesheet {} submitted status={}", id, data.status());
        return TmsApiResponse.success(TmsMessages.TIMESHEET_SUBMITTED, data);
    }

    /**
     * Delete a timesheet and its entries.
     *
     * @param id timesheet id
     * @return success response
     */
    @Operation(summary = "Delete Timesheet", description = "Delete a timesheet and its associated entries by ID.")
    @DeleteMapping("/timesheets/{id}")
    public TmsApiResponse<Void> delete(@PathVariable int id) {
        log.info("DELETE /api/v1/timesheets/{}", id);

        tmsTimesheetService.delete(id);

        log.debug("Deleted timesheet id={}", id);
        return TmsApiResponse.success(TmsMessages.TIMESHEET_DELETED, null);
    }

    /**
     * Get all timesheets in the system.
     *
     * @return list of timesheets wrapped in TmsApiResponse
     */
    @Operation(summary = "Get All Timesheets", description = "Retrieve all timesheets with details.")
    @GetMapping("/timesheets")
    public TmsApiResponse<List<TmsTimesheetDto>> getAll() {
        log.info("GET /api/v1/timesheets");

        var data = tmsTimesheetService.getAll();

        log.debug("Fetched {} timesheets", data == null ? 0 : data.size());
        return TmsApiResponse.success(TmsMessages.TIMESHEETS_FETCHED, data);
    }

    // ----------------------------
    // Bulk upsert and entry endpoints
    // ----------------------------

    /**
     * Bulk upsert (insert/update/delete) entries for a timesheet.
     *
     * @param timesheetId timesheet id path parameter
     * @param req bulk request containing entries and mode
     * @return BulkUpsertDto summary wrapped in TmsApiResponse
     */
    @Operation(summary = "Bulk Upsert Entries", description = "Insert, update or delete multiple entries in a timesheet.")
    @PutMapping("/timesheets/{id}/entries")
    public TmsApiResponse<BulkUpsertDto> bulkUpsert(
            @PathVariable("id") int timesheetId,
            @Valid @RequestBody BulkUpsertDto req) {

        log.info("PUT /api/v1/timesheets/{}/entries incomingRows={}", timesheetId, req == null || req.entries() == null ? 0 : req.entries().size());

        var data = tmsTimesheetService.bulkUpsert(timesheetId, req);

        log.debug("Bulk upsert for timesheet {} -> inserted={} updated={} deleted={} totalHours={}",
                timesheetId, data.inserted(), data.updated(), data.deleted(), data.totalHours());

        return TmsApiResponse.success(TmsMessages.BULK_UPSERT_COMPLETED, data);
    }

    /**
     * Create a new time entry under a timesheet.
     *
     * @param req time entry DTO request
     * @return created time entry DTO wrapped in TmsApiResponse
     */
    @Operation(summary = "Create Time Entry", description = "Create a new time entry under an existing timesheet.")
    @PostMapping("/time-entries")
    public TmsApiResponse<TimeEntryDto> createEntry(
            @Valid @RequestBody TimeEntryDto req) {

        log.info("POST /api/v1/time-entries timesheetId={} entryDate={} hours={}", req.timesheetId(), req.entryDate(), req.hours());

        var data = tmsTimeEntryService.create(req);

        log.debug("Created time entry id={} timesheetId={} hours={}", data.id(), data.timesheetId(), data.hours());
        return TmsApiResponse.created(TmsMessages.TIME_ENTRY_CREATED, data);
    }

    /**
     * Delete a specific time entry.
     *
     * @param entryId entry id path parameter
     * @return success response
     */
    @Operation(summary = "Delete Time Entry", description = "Delete a specific time entry by ID.")
    @DeleteMapping("/time-entries/{entryId}")
    public TmsApiResponse<Void> deleteEntry(@PathVariable("entryId") int entryId) {
        log.info("DELETE /api/v1/time-entries/{}", entryId);

        tmsTimeEntryService.delete(entryId);

        log.debug("Deleted time entry id={}", entryId);
        return TmsApiResponse.success(TmsMessages.TIME_ENTRY_DELETED,null);
    }

    /**
     * Get all time entries across all timesheets.
     *
     * @return list of time entry DTOs wrapped in TmsApiResponse
     */
    @Operation(summary = "Get All Time Entries", description = "Retrieve all time entries across all timesheets.")
    @GetMapping("/time-entries")
    public TmsApiResponse<List<TimeEntryDto>> getAllTimeEntries() {
        log.info("GET /api/v1/time-entries");

        var data = tmsTimeEntryService.getAll();

        log.debug("Fetched {} time-entries", data == null ? 0 : data.size());
        return TmsApiResponse.success(TmsMessages.ENTRIES_FETCHED, data);
    }

    // ----------------------------
    // Weekly / Monthly / Project-level aggregated endpoints
    // ----------------------------

    /**
     * Get weekly (Mon-Sun) aggregated time entries per project for a user.
     *
     * @param userEmail required user email address
     * @return map keyed by projectId containing projectName, timesheetID, status, and timeentries (date,hours,taskId,taskName)
     */
    @Operation(summary = "Get weekly time entries", description = "Return weekly (Mon-Sun) time entries aggregated per project for the specified user.")
    @GetMapping("/weekly")
    public TmsApiResponse<Map<Integer, Map<String, Object>>> weekly(
            @RequestParam("userEmail") String userEmail) {

        log.info("GET /api/v1/weekly userEmail={}", userEmail);

        var resp = tmsTimesheetService.getWeeklyTimeEntries(userEmail);

        log.debug("Weekly response prepared for user={} projects={}", userEmail, resp == null ? 0 : resp.size());
        return TmsApiResponse.success(TmsMessages.TIMESHEET_FETCHED, resp);
    }

    /**
     * Get monthly (1st to last) aggregated time entries per project for a user.
     *
     * @param userEmail required user email address
     * @return map keyed by projectId containing projectName, timesheetID, status, and timeentries (date,hours,taskId,taskName)
     */
    @Operation(summary = "Get monthly time entries", description = "Return current-month time entries aggregated per project for the specified user.")
    @GetMapping("/monthly")
    public TmsApiResponse<Map<Integer, Map<String, Object>>> monthly(
            @RequestParam("userEmail") String userEmail) {

        log.info("GET /api/v1/monthly userEmail={}", userEmail);

        var resp = tmsTimesheetService.getMonthlyTimeEntries(userEmail);

        log.debug("Monthly response prepared for user={} projects={}", userEmail, resp == null ? 0 : resp.size());
        return TmsApiResponse.success(TmsMessages.TIMESHEET_FETCHED, resp);
    }

    /**
     * Get day-by-day entries for a single project within a date range.
     *
     * @param projectId project id path parameter
     * @param start     start date (yyyy-MM-dd)
     * @param end       end date (yyyy-MM-dd)
     * @return project-level map containing projectName, timesheetID, status, and timeentries (date,hours,taskId,taskName)
     */
    @Operation(summary = "Get entries by project", description = "Return day-by-day entries for a project within the given date range.")
    @GetMapping("/project/{projectId}")
    public TmsApiResponse<Map<String, Object>> byProject(
            @PathVariable int projectId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        log.info("GET /api/v1/project/{} start={} end={}", projectId, start, end);

        var resp = tmsTimesheetService.getTimeEntriesByProject(projectId, start, end);

        log.debug("Project {} response rows={}", projectId, resp == null ? 0 : ((List<?>) resp.getOrDefault("timeentries", List.of())).size());
        return TmsApiResponse.success(TmsMessages.TIMESHEET_FETCHED, resp);
    }
}
