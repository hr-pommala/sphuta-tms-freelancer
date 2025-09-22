package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Unified DTO for Timesheet operations.
 *
 * <p>Only request and response fields that belong to a timesheet itself.
 * Filtering/pagination params have been removed.</p>
 */
public record TmsTimesheetDto(

        // ---------------- REQUEST FIELDS ----------------

        @NotNull
        @Schema(example = "501", description = "Project ID for creating timesheet", accessMode = Schema.AccessMode.READ_WRITE)
        Integer projectId,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(type = "string", example = "2025-09-01", description = "Timesheet start date", accessMode = Schema.AccessMode.READ_WRITE)
        LocalDate periodStart,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(type = "string", example = "2025-09-15", description = "Timesheet end date", accessMode = Schema.AccessMode.READ_WRITE)
        LocalDate periodEnd,

        // ---------------- RESPONSE FIELDS ----------------

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer timesheetId,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String projectName,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        TimesheetStatus status,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        List<TimeEntryDto> entries,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        List<TmsDailyTotal> dailyTotals,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        BigDecimal totalHours
) {}
