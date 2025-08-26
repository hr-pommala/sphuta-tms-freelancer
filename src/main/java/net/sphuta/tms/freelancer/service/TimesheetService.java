/*
 * =====================================================================================
 *  TimesheetService Interface
 *  -------------------------------------------------------------------------------------
 *  Purpose:
 *    - Defines the contract for Timesheet-related operations in the Freelancer TMS.
 *    - Covers listing, creation, retrieval, submission (approve), bulk upsert of entries,
 *      and administrative locking.
 *
 *  Commenting levels included as requested:
 *    1) Class-level comments (this block + Javadoc on the interface).
 *    2) Element-level comments (Javadoc on each method and notes on parameters/returns).
 *    3) Block-level comments (guidance on logging blocks to be used in implementations).
 *
 *  Logging Guidance (to be implemented in the service implementation class):
 *    - Use SLF4J (LoggerFactory.getLogger(getClass())).
 *    - Recommended levels:
 *        DEBUG: method entry/exit with input filters/ids, computed counts, and timing.
 *        INFO: state transitions (e.g., DRAFT -> APPROVED), lock operations, creation.
 *        WARN: client-caused anomalies (validation warnings if handled gracefully).
 *        ERROR: unexpected exceptions (rethrow after logging in a global handler).
 *    - Suggested message format:
 *        "[TimesheetService] <operation>: <key-info> (traceId={}, userId={})"
 *      Include correlation IDs if available via MDC.
 * =====================================================================================
 */

package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.response.PageResponse;
import net.sphuta.tms.freelancer.dto.TimeEntryDtos;
import net.sphuta.tms.freelancer.dto.TimesheetDtos;

import java.util.UUID;

/**
 * Service contract for Timesheet operations.
 *
 * <p><strong>Logging (to be added in implementation):</strong></p>
 * <ul>
 *   <li>DEBUG: log method entry with parameters and method exit with brief result summary.</li>
 *   <li>INFO: log important state changes such as submission (approval) and locking actions.</li>
 *   <li>ERROR: log unexpected failures (let a global exception handler map to HTTP responses).</li>
 * </ul>
 *
 * <p><strong>Validation & Exceptions (handled in implementation):</strong></p>
 * <ul>
 *   <li>List filters: validate page/size bounds and date ranges.</li>
 *   <li>Create: ensure periodEnd ≥ periodStart; enforce uniqueness (projectId + period).</li>
 *   <li>Get: throw NotFound if the timesheet does not exist.</li>
 *   <li>Submit: idempotent approval; ensure state transition rules.</li>
 *   <li>Bulk upsert: validate dates within period, hours &gt; 0, respect LOCKED sheets.</li>
 *   <li>Lock: change status to LOCKED; subsequent mutations must be rejected by impl.</li>
 * </ul>
 */
public interface TimesheetService {

    /**
     * List timesheets with optional filters and pagination.
     *
     * <p><strong>Inputs:</strong> {@link TimesheetDtos.TimesheetFilters} may include projectId,
     * status, date range (from/to), page, and size.</p>
     *
     * <p><strong>Returns:</strong> A {@link PageResponse} of {@link TimesheetDtos.TimesheetSummary}
     * records.</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering list(filters={})
     * // DEBUG: repository query timing & result count
     * // DEBUG: exiting list(page.number={}, page.size={}, totalElements={})
     * </pre>
     */
    PageResponse<TimesheetDtos.TimesheetSummary> list(TimesheetDtos.TimesheetFilters f);

    /**
     * Create a new timesheet header for a given project and period.
     *
     * <p><strong>Inputs:</strong> {@link TimesheetDtos.TimesheetCreateRequest} containing
     * projectId, periodStart, periodEnd.</p>
     *
     * <p><strong>Returns:</strong> {@link TimesheetDtos.TimesheetDetail} of the created timesheet.</p>
     *
     * <p><strong>Validation (implementation hint):</strong> periodEnd must be ≥ periodStart;
     * enforce uniqueness on (projectId, periodStart, periodEnd).</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering create(projectId={}, periodStart={}, periodEnd={})
     * // INFO : created timesheet id={}
     * // DEBUG: exiting create(id={})
     * </pre>
     */
    TimesheetDtos.TimesheetDetail create(TimesheetDtos.TimesheetCreateRequest req);

    /**
     * Get a single timesheet with entries and computed totals.
     *
     * <p><strong>Inputs:</strong> {@link UUID} timesheet identifier.</p>
     *
     * <p><strong>Returns:</strong> {@link TimesheetDtos.TimesheetDetail} including entries,
     * dailyTotals, and totalHours.</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering get(id={})
     * // DEBUG: exiting get(id={}, entriesCount={}, totalHours={})
     * </pre>
     */
    TimesheetDtos.TimesheetDetail get(UUID id);

    /**
     * Submit (approve) a timesheet. For freelancer flow this is idempotent and immediately
     * transitions to APPROVED.
     *
     * <p><strong>Inputs:</strong> {@link UUID} timesheet identifier.</p>
     *
     * <p><strong>Returns:</strong> Updated {@link TimesheetDtos.TimesheetDetail} with status=APPROVED.</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering submit(id={})
     * // INFO : approved timesheet id={}
     * // DEBUG: exiting submit(id={}, status=APPROVED)
     * </pre>
     */
    TimesheetDtos.TimesheetDetail submit(UUID id);

    /**
     * Bulk upsert time entries into a timesheet (insert or update rows).
     *
     * <p><strong>Inputs:</strong> timesheetId and {@link TimeEntryDtos.BulkUpsertRequest} containing
     * a list of entries and mode ("UPSERT", "INSERT_ONLY", or "UPDATE_ONLY").</p>
     *
     * <p><strong>Returns:</strong> {@link TimeEntryDtos.BulkUpsertResponse} summarizing inserted,
     * updated, deleted (if applicable), and new totalHours.</p>
     *
     * <p><strong>Validation (implementation hint):</strong> reject entries outside the timesheet
     * period; reject hours ≤ 0; reject when timesheet is LOCKED.</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering bulkUpsert(timesheetId={}, entriesCount={}, mode={})
     * // INFO : bulkUpsert summary -> inserted={}, updated={}, deleted={}, totalHours={}
     * // DEBUG: exiting bulkUpsert(timesheetId={})
     * </pre>
     */
    TimeEntryDtos.BulkUpsertResponse bulkUpsert(UUID timesheetId, TimeEntryDtos.BulkUpsertRequest req);

    /**
     * Lock a timesheet to prevent further modifications (typically post-invoicing).
     *
     * <p><strong>Inputs:</strong> {@link UUID} timesheet identifier.</p>
     *
     * <p><strong>Behavior (implementation hint):</strong> change status to LOCKED; subsequent
     * mutations should return 409 Conflict.</p>
     *
     * <p><strong>Logging (implementation hint):</strong></p>
     * <pre>
     * // DEBUG: entering lock(id={})
     * // INFO : locked timesheet id={}
     * // DEBUG: exiting lock(id={})
     * </pre>
     */
    void lock(UUID id);
}
