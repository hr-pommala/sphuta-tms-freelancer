package net.sphuta.tms.freelancer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for bulk upsert time entry operations.
 *
 * <p>Used for request and response. The 'entries' field is READ_WRITE so Swagger shows it
 * for input (request) and output (response).</p>
 */
public record BulkUpsertDto(

        @Schema(description = "List of time entries to insert/update",
                accessMode = Schema.AccessMode.READ_WRITE,
                required = true)
        List<TimeEntryDto> entries,

        @Schema(description = "Operation mode for bulk upsert. Allowed: UPSERT, INSERT_ONLY, UPDATE_ONLY",
                allowableValues = {"UPSERT", "INSERT_ONLY", "UPDATE_ONLY"},
                accessMode = Schema.AccessMode.WRITE_ONLY,
                required = true)
        @Pattern(regexp = "UPSERT|INSERT_ONLY|UPDATE_ONLY")
        String mode,

        @Schema(description = "Number of entries successfully inserted", accessMode = Schema.AccessMode.READ_ONLY)
        Integer inserted,

        @Schema(description = "Number of entries successfully updated", accessMode = Schema.AccessMode.READ_ONLY)
        Integer updated,

        @Schema(description = "Number of entries successfully deleted", accessMode = Schema.AccessMode.READ_ONLY)
        Integer deleted,

        @Schema(description = "Total hours across all entries after the bulk operation", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalHours
) {}
