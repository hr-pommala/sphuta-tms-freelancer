package net.sphuta.tms.util;

import net.sphuta.tms.dto.TimeEntryDtos;
import net.sphuta.tms.dto.TimesheetDtos;
import net.sphuta.tms.entity.TimeEntry;
import net.sphuta.tms.entity.Timesheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manual mappers + aggregations (total hours, daily totals).
 *
 * <p>This utility class converts JPA entities to DTO projections that the API returns,
 * and provides lightweight aggregation helpers used by response builders.</p>
 *
 * <p><strong>Logging:</strong> Each method logs at DEBUG level on entry/exit and
 * at TRACE level for fine-grained details. No functional changes.</p>
 */
public class Mappers {

    /** Class logger (no side effects; safe for static utility methods). */
    private static final Logger log = LoggerFactory.getLogger(Mappers.class);

    /**
     * Builds a lightweight {@link TimesheetDtos.TimesheetSummary} from a {@link Timesheet} entity.
     *
     * @param t the source timesheet entity (must be non-null; caller ensured)
     * @return summary DTO with computed total hours
     */
    public static TimesheetDtos.TimesheetSummary toSummary(Timesheet t) {
        if (log.isDebugEnabled()) {
            log.debug("toSummary() - mapping Timesheet id={} projectId={} period=[{}..{}] status={}",
                    t.getId(), t.getProjectId(), t.getPeriodStart(), t.getPeriodEnd(), t.getStatus());
        }

        // NOTE: Project name is a stub; replace when integrating with Projects service.
        TimesheetDtos.TimesheetSummary dto = new TimesheetDtos.TimesheetSummary(
                t.getId(),
                t.getProjectId(),
                "Project-" + t.getProjectId().toString().substring(0, 8), // stub project name
                t.getPeriodStart(),
                t.getPeriodEnd(),
                t.getStatus(),
                totalHours(t) // compute aggregate hours
        );

        if (log.isTraceEnabled()) {
            log.trace("toSummary() - built DTO: {}", dto);
        }
        return dto;
    }

    /**
     * Builds a detailed {@link TimesheetDtos.TimesheetDetail} including entry list,
     * daily totals, and overall total hours.
     *
     * @param t the source timesheet entity
     * @return detail DTO for API consumers
     */
    public static TimesheetDtos.TimesheetDetail toDetail(Timesheet t) {
        if (log.isDebugEnabled()) {
            log.debug("toDetail() - mapping Timesheet id={} with {} entries",
                    t.getId(), (t.getEntries() == null ? 0 : t.getEntries().size()));
        }

        // Map each entity row to an item DTO.
        var entries = t.getEntries().stream()
                .map(Mappers::toItem)
                .toList();

        // Compute daily totals from the mapped items.
        var dailyTotals = computeDailyTotals(entries);

        // Assemble the detail DTO.
        TimesheetDtos.TimesheetDetail dto = new TimesheetDtos.TimesheetDetail(
                t.getId(),
                t.getProjectId(),
                "Project-" + t.getProjectId().toString().substring(0, 8),
                t.getPeriodStart(),
                t.getPeriodEnd(),
                t.getStatus(),
                entries,
                dailyTotals,
                totalHours(t)
        );

        if (log.isTraceEnabled()) {
            log.trace("toDetail() - built DTO: {}", dto);
        }
        return dto;
    }

    /**
     * Maps a {@link TimeEntry} entity to a {@link TimesheetDtos.TimeEntryItem}.
     *
     * @param e the source time entry
     * @return a DTO item containing core entry fields
     */
    public static TimesheetDtos.TimeEntryItem toItem(TimeEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("toItem() - mapping TimeEntry id={} entryDate={} hours={}",
                    e.getId(), e.getEntryDate(), e.getHours());
        }

        TimesheetDtos.TimeEntryItem dto = new TimesheetDtos.TimeEntryItem(
                e.getId(),
                e.getEntryDate(),
                e.getDescription(),
                e.getHours(),
                e.getRateAtEntry(),
                e.getCostAtEntry()
        );

        if (log.isTraceEnabled()) {
            log.trace("toItem() - built DTO: {}", dto);
        }
        return dto;
    }

    /**
     * Sums the hours of all entries attached to a timesheet.
     *
     * @param t timesheet whose entries will be aggregated
     * @return total hours as {@link BigDecimal} (never null)
     */
    public static BigDecimal totalHours(Timesheet t) {
        if (log.isDebugEnabled()) {
            log.debug("totalHours() - computing total for Timesheet id={} entries={}",
                    t.getId(), (t.getEntries() == null ? 0 : t.getEntries().size()));
        }

        // Reduce hours across all entries; default to ZERO when empty.
        BigDecimal total = t.getEntries().stream()
                .map(TimeEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (log.isTraceEnabled()) {
            log.trace("totalHours() - result: {}", total);
        }
        return total;
    }

    /**
     * Groups entry items by date and sums their hours to produce daily totals.
     *
     * @param entries list of item DTOs (already mapped)
     * @return ordered list of daily totals (ascending by date)
     */
    public static List<TimesheetDtos.DailyTotal> computeDailyTotals(List<TimesheetDtos.TimeEntryItem> entries) {
        if (log.isDebugEnabled()) {
            log.debug("computeDailyTotals() - entries size={}", (entries == null ? 0 : entries.size()));
        }

        // Use a TreeMap to maintain natural date ordering.
        Map<LocalDate, BigDecimal> map = new TreeMap<>();

        // Aggregate hours by date.
        for (var e : entries) {
            // Merge hours for the current date; add if absent.
            map.merge(e.entryDate(), e.hours(), BigDecimal::add);
        }

        // Convert map entries to DTO list.
        List<TimesheetDtos.DailyTotal> totals = map.entrySet().stream()
                .map(kv -> new TimesheetDtos.DailyTotal(kv.getKey(), kv.getValue()))
                .collect(Collectors.toList());

        if (log.isTraceEnabled()) {
            log.trace("computeDailyTotals() - daily totals: {}", totals);
        }
        return totals;
    }

    /**
     * Maps a {@link TimeEntry} entity to a {@link TimeEntryDtos.TimeEntryResponse}.
     *
     * @param e the source time entry
     * @return response DTO exposing entry snapshot values
     */
    public static TimeEntryDtos.TimeEntryResponse toEntryResponse(TimeEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("toEntryResponse() - mapping TimeEntry id={} rate={} cost={}",
                    e.getId(), e.getRateAtEntry(), e.getCostAtEntry());
        }

        TimeEntryDtos.TimeEntryResponse dto = new TimeEntryDtos.TimeEntryResponse(
                e.getId(),
                e.getTimesheet().getId(),
                e.getEntryDate(),
                e.getDescription(),
                e.getHours(),
                e.getRateAtEntry(),
                e.getCostAtEntry()
        );

        if (log.isTraceEnabled()) {
            log.trace("toEntryResponse() - built DTO: {}", dto);
        }
        return dto;
    }
}
