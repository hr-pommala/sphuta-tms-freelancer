package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * ==========================================================
 * TmsInvoiceDto
 * ==========================================================
 *
 * Unified DTO for Invoice operations (create, update, read).
 *
 * Purpose:
 * - Used for both API requests (create/update) and responses (read).
 * - Combines request validations with system-managed response fields.
 *
 * Notes:
 * - {@code id}, {@code status}, {@code createdAt}, and {@code updatedAt} are read-only.
 * - {@code timeEntryIds} is required only when creating an invoice.
 * - Business logic may enforce {@code dueDate >= issueDate}.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsInvoiceDto", description = "Unified DTO for invoice requests and responses")
public record TmsInvoiceDto(

        /** Unique identifier of the invoice (system-managed, response only). */
        @Schema(description = "Invoice ID (auto-generated)", example = "2001", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        /** Client associated with the invoice. */
        @NotNull
        @Schema(description = "Client ID (must refer to an existing client)", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer clientId,

        /** Date when the invoice was issued. */
        @NotNull
        @Schema(description = "Issue date (YYYY-MM-DD)", example = "2025-08-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate issueDate,

        /** Date when the invoice is due. */
        @NotNull
        @Schema(description = "Due date (YYYY-MM-DD)", example = "2025-09-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dueDate,

        /** Currency used for the invoice, ISO-4217 (3 uppercase letters). */
        @Pattern(regexp = "^[A-Z]{3}$")
        @Schema(description = "Currency code (ISO-4217, 3 uppercase letters)", example = "USD")
        String currencyCode,

        /** Current status of the invoice (system-managed, response only). */
        @Schema(description = "Invoice status (e.g., DRAFT, SENT, PAID)", example = "DRAFT", accessMode = Schema.AccessMode.READ_ONLY)
        String status,

        /** Optional notes displayed on the invoice. */
        @Schema(description = "Optional notes (max 1000 chars)", maxLength = 1000, example = "Payment due in 30 days")
        String notes,

        /** IDs of approved time entries to be included in the invoice (required on create). */
        @NotEmpty
        @Schema(description = "List of time entry IDs to include in the invoice (required for create)", example = "[5001, 5002]")
        List<Integer> timeEntryIds,

        /** Timestamp when the invoice was created (system-managed, response only). */
        @Schema(description = "Record creation timestamp", example = "2025-08-28T11:44:28Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime createdDt,

        /** Timestamp when the invoice was last updated (system-managed, response only). */
        @Schema(description = "Record last updated timestamp", example = "2025-08-29T15:22:10Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime updatedDt
) {}
