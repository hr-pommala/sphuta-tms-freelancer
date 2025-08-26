package net.sphuta.tms.freelancer.dto;

import jakarta.validation.constraints.*;
import net.sphuta.tms.freelancer.enam.TimesheetStatus; // NOTE: keeping your package path as-is per request

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Timesheet-related Data Transfer Objects (DTOs).
 *
 * <p>These Java 17 {@code record}s are used for request/response shapes across the Timesheet API.
 * Validation annotations here (@NotNull, @Min, @Max, etc.) are enforced by Spring MVC when
 * the DTOs are used as controller parameters.</p>
 *
 * <h2>Usage overview</h2>
 * <ul>
 *   <li>{@link TimesheetFilters} — captures query parameters for list/search endpoints.</li>
 *   <li>{@link TimesheetCreateRequest} — payload for creating a new timesheet header.</li>
 *   <li>{@link TimesheetSummary} — lightweight projection for list pages.</li>
 *   <li>{@link TimesheetDetail} — detailed projection, including entries and daily totals.</li>
 *   <li>{@link TimeEntryItem} — single time-entry row in the detail view.</li>
 *   <li>{@link DailyTotal} — summarized hours per day for week-grid totals.</li>
 * </ul>
 *
 * <p><b>Note on logging:</b> DTOs intentionally contain no methods/logic, so there is nothing to log here.
 * Add logs in controllers/services around the use of these DTOs.</p>
 */
public class TimesheetDtos {

    /* -------------------------------------------------------------------------
     * Filters captured via @ModelAttribute from query params (List/GET endpoint)
     * ------------------------------------------------------------------------- */
    public record TimesheetFilters(

            /** Optional filter — restrict results to a specific project. */
            UUID projectId,

            /** Optional filter — restrict by timesheet status (e.g., DRAFT, APPROVED, LOCKED). */
            TimesheetStatus status,

            /** Optional filter — include timesheets whose period starts on/after this date. */
            LocalDate from,

            /** Optional filter — include timesheets whose period ends on/before this date. */
            LocalDate to,

            /** Page number (0-based). Must be >= 0. */
            @Min(0) Integer page,

            /** Page size limit. Must be within [1, 200] to guard against excessive payloads. */
            @Min(1) @Max(200) Integer size
    ) {}

    /* -------------------------------------------------------------
     * Create request via @RequestBody (POST /timesheets)
     * ------------------------------------------------------------- */
    public record TimesheetCreateRequest(

            /** The target project identifier for which the timesheet is created. */
            @NotNull UUID projectId,

            /** Inclusive start date of the timesheet period (e.g., week start). */
            @NotNull LocalDate periodStart,

            /** Inclusive end date of the timesheet period (must be >= start). */
            @NotNull LocalDate periodEnd
    ) {}

    /* -------------------------------------------------------------
     * List view projection (used in paged list endpoint responses)
     * ------------------------------------------------------------- */
    public record TimesheetSummary(

            /** Timesheet identifier. */
            UUID id,

            /** Associated project identifier. */
            UUID projectId,

            /** Human-friendly project name (projection-only field). */
            String projectName,

            /** Period start date for this timesheet. */
            LocalDate periodStart,

            /** Period end date for this timesheet. */
            LocalDate periodEnd,

            /** Current status of the timesheet (DRAFT/APPROVED/LOCKED). */
            TimesheetStatus status,

            /** Total hours across all entries in this timesheet. */
            BigDecimal totalHours
    ) {}

    /* -------------------------------------------------------------------------
     * Detail view projection (header + entries + daily totals + total hours)
     * ------------------------------------------------------------------------- */
    public record TimesheetDetail(

            /** Timesheet identifier. */
            UUID id,

            /** Associated project identifier. */
            UUID projectId,

            /** Human-friendly project name (projection-only field). */
            String projectName,

            /** Period start date for this timesheet. */
            LocalDate periodStart,

            /** Period end date for this timesheet. */
            LocalDate periodEnd,

            /** Current status of the timesheet (DRAFT/APPROVED/LOCKED). */
            TimesheetStatus status,

            /** Flat list of time-entry items that belong to this timesheet. */
            List<TimeEntryItem> entries,

            /** Summed hours per day across the period window. */
            List<DailyTotal> dailyTotals,

            /** Total hours across all entries. */
            BigDecimal totalHours
    ) {}

    /* -------------------------------------------------------------
     * Entry item as presented in a detailed timesheet view
     * ------------------------------------------------------------- */
    public record TimeEntryItem(

            /** Unique identifier of the time entry row. */
            UUID id,

            /** The calendar date for which this entry was recorded. */
            LocalDate entryDate,

            /** Free-text description of the work performed. */
            String description,

            /** Hours booked for this entry (e.g., 1.50, 2.00, etc.). */
            BigDecimal hours,

            /** Rate captured at time of entry (snapshot for historical reporting). */
            BigDecimal rateAtEntry,

            /** Cost captured at time of entry (normally hours × rateAtEntry). */
            BigDecimal costAtEntry
    ) {}

    /* -------------------------------------------------------------
     * Aggregated daily total line for the week-grid
     * ------------------------------------------------------------- */
    public record DailyTotal(

            /** The date that this total applies to. */
            LocalDate date,

            /** Sum of hours across entries that fall on {@code date}. */
            BigDecimal hours
    ) {}
}
