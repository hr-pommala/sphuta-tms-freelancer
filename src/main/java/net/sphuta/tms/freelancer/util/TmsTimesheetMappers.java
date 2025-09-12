package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.*;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between {@link TimesheetEntity}, {@link TimeEntryEntity}
 * and their respective DTOs.
 */
@Slf4j
public class TmsTimesheetMappers {

    public static TmsTimesheetDto toDetail(TimesheetEntity t) {
        var entries = t.getEntries().stream()
                .map(TmsTimesheetMappers::toTimeEntryDto)
                .toList();

        var dailyTotals = computeDailyTotals(entries);

        var dto = new TmsTimesheetDto(
                t.getProjectId(),
                t.getPeriodStart(),
                t.getPeriodEnd(),
                t.getId(),
                "Project-" + t.getProjectId(), // placeholder projectName
                t.getStatus(),
                entries,
                dailyTotals,
                totalHours(t)
        );

        log.debug("Mapped TimesheetEntity(id={}) to DTO: {}", t.getId(), dto);
        return dto;
    }

    public static TimeEntryDto toTimeEntryDto(TimeEntryEntity e) {
        var dto = new TimeEntryDto(
                e.getTimesheet().getId(),
                e.getEntryDate(),
                e.getDescription(),
                e.getHours(),
                e.getRateAtEntry(),
                e.getId(),
                e.getCostAtEntry()
        );
        log.trace("Mapped TimeEntryEntity(id={}) to DTO: {}", e.getId(), dto);
        return dto;
    }

    public static BigDecimal totalHours(TimesheetEntity t) {
        return t.getEntries().stream()
                .map(TimeEntryEntity::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static List<TmsDailyTotal> computeDailyTotals(List<TimeEntryDto> entries) {
        Map<LocalDate, BigDecimal> byDate = new TreeMap<>();
        for (var e : entries) {
            byDate.merge(e.entryDate(), e.hours(), BigDecimal::add);
        }
        return byDate.entrySet().stream()
                .map(kv -> new TmsDailyTotal(kv.getKey(), kv.getValue()))
                .collect(Collectors.toList());
    }

    public static TimeEntryDto toEntryResponse(TimeEntryEntity e) {
        return toTimeEntryDto(e);
    }

    /**
     * ✅ Updated: Ensures response always has proper IDs and costAtEntry.
     *
     * <p>This method takes the list of processed entries (inserted/updated),
     * ensures that each entry has a non-null ID and costAtEntry computed,
     * and wraps them in a {@link BulkUpsertDto} for returning to the client.</p>
     *
     * @param processed list of processed time entries (raw mapped DTOs)
     * @param mode the mode used in bulk operation (UPSERT, INSERT_ONLY, UPDATE_ONLY)
     * @param inserted number of inserted entries
     * @param updated number of updated entries
     * @param deleted number of deleted entries (always 0 in current flow)
     * @param totalHours total hours across all entries after operation
     * @return {@link BulkUpsertDto} safe response for API
     */
    public static BulkUpsertDto toBulkUpsertResponse(
            List<TimeEntryDto> processed, String mode,
            int inserted, int updated, int deleted, BigDecimal totalHours) {

        log.info("Building BulkUpsertDto response: inserted={}, updated={}, deleted={}, totalHours={}",
                inserted, updated, deleted, totalHours);

        // Ensure each entry is safely mapped with proper id and costAtEntry
        List<TimeEntryDto> safeEntries = processed.stream()
                .map(e -> {
                    // Log before mapping
                    log.debug("Mapping entry for response -> timesheetId={}, date={}, desc='{}', hours={}, rate={}, id={}, cost={}",
                            e.timesheetId(), e.entryDate(), e.description(), e.hours(), e.rateAtEntry(), e.id(), e.costAtEntry());

                    // Create safe DTO with fallback for id and recalculated cost
                    TimeEntryDto dto = new TimeEntryDto(
                            e.timesheetId(),
                            e.entryDate(),
                            e.description(),
                            e.hours(),
                            e.rateAtEntry(),
                            Optional.ofNullable(e.id()).orElse(-1), // fallback to -1 if id is null
                            Optional.ofNullable(e.rateAtEntry())
                                    .filter(rate -> e.hours() != null)
                                    .map(rate -> rate.multiply(e.hours()))
                                    .orElse(e.costAtEntry())
                    );

                    log.trace("Mapped safe entry DTO: {}", dto);
                    return dto;
                })
                .toList();

        // Final wrap into BulkUpsertDto
        BulkUpsertDto response = new BulkUpsertDto(
                safeEntries,
                mode,
                inserted,
                updated,
                deleted,
                totalHours
        );

        log.info("BulkUpsertDto response built successfully with {} entries", safeEntries.size());
        return response;
    }

    /** Map list of TimesheetEntity -> List<TmsTimesheetDto> */
    public static List<TmsTimesheetDto> toTimesheetResponseList(List<TimesheetEntity> entities) {
        return Optional.ofNullable(entities)
                .filter(e -> !e.isEmpty())
                .map(list -> list.stream().map(TmsTimesheetMappers::toDetail).toList())
                .orElse(Collections.emptyList());
    }

    /** Map list of TimeEntryEntity -> List<TimeEntryDto> */
    public static List<TimeEntryDto> toEntryResponseList(List<TimeEntryEntity> entities) {
        return Optional.ofNullable(entities)
                .filter(e -> !e.isEmpty())
                .map(list -> list.stream().map(TmsTimesheetMappers::toEntryResponse).toList())
                .orElse(Collections.emptyList());
    }

}
