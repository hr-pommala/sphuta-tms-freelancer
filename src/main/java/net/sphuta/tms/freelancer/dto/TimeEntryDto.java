package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for TimeEntry operations.
 *
 * <p>Request fields are marked WRITE_ONLY, so Swagger will only show them in requests.
 * Response fields are marked READ_ONLY, so Swagger will only show them in responses.</p>
 */
public record TimeEntryDto(

        // -------- REQUEST FIELDS --------

        @NotNull(message = "Timesheet ID is required")
        @Schema(example = "101", description = "Target timesheet ID", accessMode = Schema.AccessMode.WRITE_ONLY)
        int timesheetId,

        @NotNull(message = "Entry date is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(type = "string", example = "2025-08-21", description = "Entry date (yyyy-MM-dd)", accessMode = Schema.AccessMode.WRITE_ONLY)
        LocalDate entryDate,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(example = "Worked on authentication endpoints", description = "Optional description (≤500 chars)", accessMode = Schema.AccessMode.WRITE_ONLY)
        String description,

        @NotNull(message = "Hours are required")
        @DecimalMin(value = "0.01", message = "Minimum 0.01 hours required")
        @DecimalMax(value = "24.00", message = "Maximum 24.00 hours allowed")
        @Schema(example = "4.0", description = "Hours worked (0.01–24.00)", accessMode = Schema.AccessMode.WRITE_ONLY)
        BigDecimal hours,

        @Positive(message = "Rate must be positive")
        @Schema(example = "65.00", description = "Hourly rate snapshot at the time of entry", accessMode = Schema.AccessMode.WRITE_ONLY)
        BigDecimal rateAtEntry,

        // -------- RESPONSE FIELDS --------

        @Schema(description = "Unique identifier of the time entry", example = "501", accessMode = Schema.AccessMode.READ_ONLY)
        int id,

        @Schema(description = "Total cost calculated at entry (hours × rateAtEntry)", example = "260.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal costAtEntry
) {}
