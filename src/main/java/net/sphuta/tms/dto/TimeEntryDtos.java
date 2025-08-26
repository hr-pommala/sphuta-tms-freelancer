package net.sphuta.tms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for time-entry operations.
 *
 * <p>This file defines the request/response shapes used by the Time Entry endpoints.
 * It intentionally contains no business logic and therefore no logging statements.
 * All validation annotations here (@NotNull, @DecimalMin, etc.) are purely declarative
 * and do not change runtime logic—only enforce constraints at the API boundary.</p>
 *
 * <p><b>Logging note:</b> DTOs are passive data carriers; logging is best placed in
 * controllers/services where methods execute. If you want, I can add SLF4J logs around
 * create/bulk-upsert/delete flows without altering behavior.</p>
 */
public class TimeEntryDtos {

    /* ----------------------------------------------------------------------
     * CREATE REQUEST (no startTime / endTime in payload)
     * ----------------------------------------------------------------------
     * The API will auto-populate start/end times at the service layer upon creation.
     */

    /**
     * Payload for creating a single time entry.
     *
     * @param timesheetId   Target timesheet identifier (UUID).
     * @param entryDate     Work date (ISO yyyy-MM-dd).
     * @param description   Optional details about the work performed (max 500 chars).
     * @param hours         Number of hours (0.01 to 24.00 inclusive).
     * @param rateAtEntry   Optional rate snapshot used to compute costAtEntry.
     */
    public record TimeEntryCreateRequest(
            @NotNull
            @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Target timesheet UUID")
            UUID timesheetId,

            @NotNull
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            @Schema(type = "string", example = "2025-08-21", description = "Entry date (yyyy-MM-dd)")
            LocalDate entryDate,

            @Size(max = 500)
            @Schema(example = "Dev - auth endpoints", description = "Optional description (≤500 chars)")
            String description,

            @DecimalMin(value = "0.01") @DecimalMax(value = "24.00")
            @Schema(example = "4.0", description = "Hours worked (0.01–24.00)")
            BigDecimal hours,

            @Schema(example = "65.00", description = "Optional rate snapshot")
            BigDecimal rateAtEntry
    ) {}

    /* ----------------------------------------------------------------------
     * BULK UPSERT REQUEST (no times in payload)
     * ----------------------------------------------------------------------
     * Inserted rows get auto-set times in the service; updates preserve existing times.
     */

    /**
     * Bulk upsert payload for multiple entries.
     *
     * @param entries List of create-like items to insert/update.
     * @param mode    Operation mode: UPSERT | INSERT_ONLY | UPDATE_ONLY.
     */
    public record BulkUpsertRequest(
            List<TimeEntryCreateRequest> entries,
            @Pattern(regexp = "UPSERT|INSERT_ONLY|UPDATE_ONLY")
            String mode
    ) {}

    /* ----------------------------------------------------------------------
     * SINGLE ENTRY RESPONSE
     * ----------------------------------------------------------------------
     * Mirrors what the API returns for a created/queried time entry.
     * (startTime/endTime are managed at the service/entity layers; not included here.)
     */

    /**
     * Response projection for a single time entry.
     *
     * @param id            Entry identifier (UUID).
     * @param timesheetId   Owning timesheet identifier (UUID).
     * @param entryDate     Work date (ISO yyyy-MM-dd).
     * @param description   Description as captured at entry time.
     * @param hours         Hours recorded for this entry.
     * @param rateAtEntry   Rate snapshot (if provided).
     * @param costAtEntry   Computed cost snapshot (rateAtEntry * hours), if available.
     */
    public record TimeEntryResponse(
            UUID id,
            UUID timesheetId,
            LocalDate entryDate,
            String description,
            BigDecimal hours,
            BigDecimal rateAtEntry,
            BigDecimal costAtEntry
    ) {}

    /* ----------------------------------------------------------------------
     * BULK UPSERT RESPONSE
     * ----------------------------------------------------------------------
     * Summarizes the effect of the bulk operation along with the new total hours.
     */

    /**
     * Result summary from a bulk upsert.
     *
     * @param inserted   Number of newly inserted rows.
     * @param updated    Number of updated rows.
     * @param deleted    Number of deleted rows (always 0 in current flow).
     * @param totalHours Recomputed total hours on the timesheet after the operation.
     */
    public record BulkUpsertResponse(
            int inserted,
            int updated,
            int deleted,
            BigDecimal totalHours
    ) {}
}
