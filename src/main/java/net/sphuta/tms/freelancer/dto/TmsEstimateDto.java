package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * ==========================================================
 * TmsEstimateDto
 * ==========================================================
 *
 * Unified DTO for Estimate operations (create, update, read).
 *
 * Purpose:
 * - Used for both API requests (create/update) and responses (read).
 * - Combines validation rules for input with system-managed fields for output.
 *
 * Notes:
 * - {@code id}, {@code createdAt}, and {@code updatedAt} are read-only (ignored on input).
 * - {@code items} must contain at least one {@link TmsEstimateItemDto}.
 * - Business logic may enforce {@code validUntil >= issueDate}.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsEstimateDto", description = "Unified DTO for estimate requests and responses")
public record TmsEstimateDto(

        /** Unique identifier of the estimate (system-managed, response only). */
        @Schema(description = "Estimate ID (auto-generated)", example = "1001", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        /** The client this estimate belongs to (required on create). */
        @NotNull
        @Schema(description = "Client ID (must refer to an existing client)", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer clientId,

        /** Date when the estimate was issued. */
        @NotNull
        @Schema(description = "Issue date (YYYY-MM-DD)", example = "2025-08-10", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate issueDate,

        /** Date until which the estimate remains valid. */
        @NotNull
        @Schema(description = "Valid-until date (YYYY-MM-DD)", example = "2025-09-10", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate validUntil,

        /** Currency in ISO-4217 format (e.g., USD, EUR). */
        @Pattern(regexp = "^[A-Z]{3}$")
        @Schema(description = "Currency code (ISO-4217, 3 uppercase letters)", example = "USD")
        String currencyCode,

        /** Optional notes displayed on the estimate. */
        @Schema(description = "Optional notes (max 1000 chars)", maxLength = 1000, example = "Valid for 30 days")
        String notes,

        /** Line items that make up the estimate; must contain at least one item. */
        @NotEmpty
        @Schema(description = "Estimate line items (must be non-empty)", requiredMode = Schema.RequiredMode.REQUIRED)
        List<@Valid TmsEstimateItemDto> items,

        /** Timestamp when this estimate was created (system-managed, response only). */
        @Schema(description = "Record creation timestamp", example = "2025-08-28T11:44:28Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime createdAt,

        /** Timestamp when this estimate was last updated (system-managed, response only). */
        @Schema(description = "Record last updated timestamp", example = "2025-08-29T15:22:10Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime updatedAt
) {}
