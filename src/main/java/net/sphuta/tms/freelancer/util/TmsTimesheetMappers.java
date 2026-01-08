package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.*;
import net.sphuta.tms.freelancer.entity.TaskEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility mapping helpers for Timesheet and TimeEntry transformations.
 *
 * Responsibilities:
 * - Convert TimesheetEntity -> TmsTimesheetDto
 * - Convert TimeEntryEntity -> TimeEntryDto (including task info if available)
 * - Compute daily totals and total hours
 * - Build compact day-by-day rows used by weekly/monthly endpoints
 *
 * Notes:
 * - All methods are static/stateless so mapping logic stays centralized.
 * - Access to the Task association is defensive (safeGetTask) so mapping doesn't fail
 *   on proxy / lazy-initialization cases.
 *
 * IMPORTANT:
 * - This mapper expects the TimeEntryDto record constructor order to be:
 *   (Integer timesheetId, LocalDate entryDate, String description, BigDecimal hours,
 *    BigDecimal rateAtEntry, Integer taskId, String taskName, Integer id, BigDecimal costAtEntry)
 */
@Slf4j
public class TmsTimesheetMappers {

    // -------------------------------------------------------------------------
    // Timesheet -> DTO
    // -------------------------------------------------------------------------

    /**
     * Convert a TimesheetEntity into a TmsTimesheetDto.
     *
     * @param t timesheet entity (not null)
     * @return TmsTimesheetDto representation
     */
    public static TmsTimesheetDto toDetail(TimesheetEntity t) {
        Objects.requireNonNull(t, "timesheet entity must not be null");

        var entries = Optional.ofNullable(t.getEntries())
                .orElse(List.of())
                .stream()
                .map(TmsTimesheetMappers::toTimeEntryDto)
                .toList();

        var dailyTotals = computeDailyTotals(entries);

        //String projectName = "Project-" + t.getProjectId();
        String projectName = t.getProject() != null ? t.getProject().getName() : "Unknown";

        var dto = new TmsTimesheetDto(
                t.getProjectId(),
                t.getPeriodStart(),
                t.getPeriodEnd(),
                t.getId(),
                projectName,
                t.getStatus(),
                entries,
                dailyTotals,
                totalHours(t)
        );

        log.debug("toDetail: mapped TimesheetEntity(id={}) -> DTO entriesCount={}", t.getId(), entries.size());
        return dto;
    }

    // -------------------------------------------------------------------------
    // TimeEntry -> DTO
    // -------------------------------------------------------------------------

    /**
     * Convert a TimeEntryEntity into TimeEntryDto.
     *
     * The TimeEntryDto constructor arguments MUST match the record declaration:
     * (timesheetId, entryDate, description, hours, rateAtEntry, taskId, taskName, id, costAtEntry)
     *
     * @param e time entry entity
     * @return TimeEntryDto
     */
    public static TimeEntryDto toTimeEntryDto(TimeEntryEntity e) {
        Objects.requireNonNull(e, "time entry entity must not be null");

        Integer taskId = null;
        String taskName = null;

        Optional<TaskEntity> maybeTask = safeGetTask(e);
        if (maybeTask.isPresent()) {
            TaskEntity task = maybeTask.get();
            taskId = task.getId();
            taskName = task.getTaskName();
            log.trace("toTimeEntryDto: entryId={} resolved taskId={} taskName={}", e.getId(), taskId, taskName);
        } else {
            log.trace("toTimeEntryDto: entryId={} task not available (null or not initialised)", e.getId());
        }

//        // derive project name (if you have a relation or service to fetch it)
//        String projectName = null;
//        if (e.getProjectId() != null) {
//            projectName = " " + e.getProjectId();
//            // if you have ProjectEntity relation: projectName = e.getProject().getName();
//        }
        String projectName = e.getTimesheet() != null && e.getTimesheet().getProject() != null
                ? e.getTimesheet().getProject().getName()
                : null;

        // IMPORTANT: Constructor order matches TimeEntryDto record definition

        return new TimeEntryDto(
                e.getTimesheet() != null ? e.getTimesheet().getId() : null,
                e.getEntryDate(),
                e.getDescription(),
                e.getHours(),
                e.getRateAtEntry(),
                taskId,
                taskName,
                e.getId(),
                e.getCostAtEntry(),
                projectName

        );
    }

    /**
     * Safe getter for TaskEntity from TimeEntryEntity.
     * Catches runtime exceptions (lazy proxies) and returns Optional.empty() in that case.
     */
    private static Optional<TaskEntity> safeGetTask(TimeEntryEntity e) {
        try {
            return Optional.ofNullable(e.getTask());
        } catch (RuntimeException ex) {
            log.debug("safeGetTask: could not read task for entryId={} -> {}", e.getId(), ex.toString());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // Totals / daily totals
    // -------------------------------------------------------------------------

    /**
     * Sum the hours across all entries of a timesheet.
     */
    public static BigDecimal totalHours(TimesheetEntity t) {
        if (t == null || t.getEntries() == null) return BigDecimal.ZERO;
        BigDecimal total = t.getEntries().stream()
                .map(TimeEntryEntity::getHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("totalHours: computed {} for timesheetId={}", total, t.getId());
        return total;
    }

    /**
     * Compute daily totals from a list of TimeEntryDto.
     */
    public static List<TmsDailyTotal> computeDailyTotals(List<TimeEntryDto> entries) {
        Map<LocalDate, BigDecimal> byDate = new TreeMap<>();
        for (var e : Optional.ofNullable(entries).orElse(List.of())) {
            if (e.entryDate() != null && e.hours() != null) {
                byDate.merge(e.entryDate(), e.hours(), BigDecimal::add);
            }
        }
        log.debug("computeDailyTotals: computed {} daily totals", byDate.size());
        return byDate.entrySet().stream()
                .map(kv -> new TmsDailyTotal(kv.getKey(), kv.getValue()))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Bulk upsert response builder
    // -------------------------------------------------------------------------

    /**
     * Build BulkUpsertDto from processed TimeEntryDto objects.
     *
     * Ensures costAtEntry is computed when possible and id is non-null (fallback -1).
     * Preserves taskId and taskName from the processed DTOs.
     */
    public static BulkUpsertDto toBulkUpsertResponse(
            List<TimeEntryDto> processed, String mode,
            int inserted, int updated, int deleted, BigDecimal totalHours) {

        log.info("toBulkUpsertResponse: building response mode={} inserted={} updated={} deleted={} totalHours={}",
                mode, inserted, updated, deleted, totalHours);

        List<TimeEntryDto> safeEntries = Optional.ofNullable(processed).orElse(List.of()).stream()
                .map(e -> {
                    BigDecimal computedCost = Optional.ofNullable(e.rateAtEntry())
                            .filter(rate -> e.hours() != null)
                            .map(rate -> rate.multiply(e.hours()))
                            .orElse(e.costAtEntry());

                    // Maintain exact constructor order expected by the TimeEntryDto record.
                    return new TimeEntryDto(
                            e.timesheetId(),
                            e.entryDate(),
                            e.description(),
                            e.hours(),
                            e.rateAtEntry(),
                            e.taskId(),
                            e.taskName(),
                            Optional.ofNullable(e.id()).orElse(-1),
                            computedCost,
                            e.projectName()


                    );
                })
                .toList();

        BulkUpsertDto response = new BulkUpsertDto(
                safeEntries,
                mode,
                inserted,
                updated,
                deleted,
                totalHours
        );

        log.info("toBulkUpsertResponse: built response with {} entries", safeEntries.size());
        return response;
    }

    // -------------------------------------------------------------------------
    // Convenience lists -> DTO lists
    // -------------------------------------------------------------------------

    public static List<TmsTimesheetDto> toTimesheetResponseList(List<TimesheetEntity> entities) {
        List<TmsTimesheetDto> list = Optional.ofNullable(entities)
                .filter(e -> !e.isEmpty())
                .map(l -> l.stream().map(TmsTimesheetMappers::toDetail).toList())
                .orElse(Collections.emptyList());
        log.debug("toTimesheetResponseList: mapped {} timesheets", list.size());
        return list;
    }

    public static List<TimeEntryDto> toEntryResponseList(List<TimeEntryEntity> entities) {
        List<TimeEntryDto> list = Optional.ofNullable(entities)
                .filter(e -> !e.isEmpty())
                .map(l -> l.stream().map(TmsTimesheetMappers::toEntryResponse).toList())
                .orElse(Collections.emptyList());
        log.debug("toEntryResponseList: mapped {} entries", list.size());
        return list;
    }

    public static TimeEntryDto toEntryResponse(TimeEntryEntity e) {
        return toTimeEntryDto(e);
    }

    // -------------------------------------------------------------------------
    // Compact day-by-day row builder used by weekly/monthly endpoints
    // -------------------------------------------------------------------------

    /**
     * Build an ordered list of compact rows for each calendar day between start and end (inclusive).
     *
     * Row keys: date (String), hours (BigDecimal), taskId (Integer|null), taskName (String|null)
     *
     * Aggregation rules:
     * - If a date has no entries → hours=0, taskId=null, taskName=null
     * - If all entries for a date reference the same non-null task → return that taskId/taskName
     * - Otherwise (mixed tasks or some entries without task) → taskId/taskName = null
     */
    public static List<Map<String, Object>> toDailyCompactRows(List<TimeEntryEntity> entries, LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start date required");
        Objects.requireNonNull(end, "end date required");
        if (end.isBefore(start)) throw new IllegalArgumentException("end must be >= start");

        Map<LocalDate, List<TimeEntryEntity>> byDate = new TreeMap<>();
        for (TimeEntryEntity e : Optional.ofNullable(entries).orElse(List.of())) {
            if (e.getEntryDate() == null) {
                log.debug("toDailyCompactRows: skipping entry id={} with null entryDate", e.getId());
                continue;
            }
            byDate.computeIfAbsent(e.getEntryDate(), d -> new ArrayList<>()).add(e);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            List<TimeEntryEntity> dayEntries = byDate.getOrDefault(cursor, List.of());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", cursor.toString());

            if (dayEntries.isEmpty()) {
                row.put("hours", BigDecimal.ZERO);
                row.put("taskId", null);
                row.put("taskName", null);
                log.trace("toDailyCompactRows: date={} -> zero-row", cursor);
            } else {
                BigDecimal total = BigDecimal.ZERO;
                Integer chosenTaskId = null;
                String chosenTaskName = null;
                boolean multipleTasks = false;

                for (TimeEntryEntity e : dayEntries) {
                    total = total.add(Optional.ofNullable(e.getHours()).orElse(BigDecimal.ZERO));
                    Optional<TaskEntity> maybeTask = safeGetTask(e);
                    if (maybeTask.isPresent()) {
                        Integer tid = maybeTask.get().getId();
                        String tname = maybeTask.get().getTaskName();
                        if (chosenTaskId == null) {
                            chosenTaskId = tid;
                            chosenTaskName = tname;
                        } else if (!Objects.equals(chosenTaskId, tid)) {
                            multipleTasks = true;
                        }
                    } else {
                        // If any entry lacks a task while another entry had one -> ambiguous
                        if (chosenTaskId != null) multipleTasks = true;
                    }
                }

                row.put("hours", total);
                if (multipleTasks) {
                    row.put("taskId", null);
                    row.put("taskName", null);
                    log.trace("toDailyCompactRows: date={} -> multiple/ambiguous tasks; hours={}", cursor, total);
                } else {
                    row.put("taskId", chosenTaskId);
                    row.put("taskName", chosenTaskName);
                    log.trace("toDailyCompactRows: date={} -> chosenTaskId={} hours={}", cursor, chosenTaskId, total);
                }
            }

            rows.add(row);
            cursor = cursor.plusDays(1);
        }

        log.debug("toDailyCompactRows: built {} rows for range {}..{}", rows.size(), start, end);
        return rows;
    }

}
