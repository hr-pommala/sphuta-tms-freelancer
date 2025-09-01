package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * ==========================================================
 * TmsClientDto
 * ==========================================================
 *
 * Unified DTO for client operations (create, update, read).
 *
 * Purpose:
 * - Combines request validation rules and response fields into a single DTO.
 * - Used for both API requests and responses.
 *
 * Notes:
 * - Validation rules apply during request (create/update).
 * - Response-only fields like `id`, `createdAt`, `updatedAt` are nullable on input.
 * - JSON excludes nulls for cleaner payloads.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsClientDto", description = "Client unified DTO for requests and responses")
public record TmsClientDto(

        /** Auto-incremented unique identifier (system-managed, response only). */
        @Schema(description = "Client ID (auto-incremented)", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        /** Primary billing email (required for requests). */
        @Email
        @NotBlank(message = "email is required")
        @Schema(description = "Primary billing email", example = "billing@acme.com")
        String email,

        /** Company name (optional, max 255 chars). */
        @Size(max = 255)
        @Schema(description = "Company name", example = "Acme LLC")
        String companyName,

        /** Contact's first name (required if no companyName). */
        @Size(max = 200)
        @Schema(description = "Contact first name", example = "John")
        String firstName,

        /** Contact's last name (required if no companyName). */
        @Size(max = 200)
        @Schema(description = "Contact last name", example = "Doe")
        String lastName,

        /** Mobile phone number. */
        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "mobilePhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Mobile phone", example = "+1-555-0100")
        String mobilePhone,

        /** Business phone number. */
        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "businessPhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Business phone", example = "+1-555-0199")
        String businessPhone,

        /** Address line 1. */
        @Size(max = 500)
        @Schema(description = "Address line 1", example = "1 Main St")
        String addressLine1,

        /** Address line 2. */
        @Size(max = 500)
        @Schema(description = "Address line 2", example = "Suite 200")
        String addressLine2,

        /** City. */
        @Size(max = 120)
        @Schema(description = "City", example = "Austin")
        String city,

        /** State or province. */
        @Size(max = 120)
        @Schema(description = "State", example = "TX")
        String state,

        /** Postal code. */
        @Size(max = 40)
        @Schema(description = "Postal code", example = "78701")
        String postalCode,

        /** ISO 3166-1 alpha-2 country code. */
        @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode must be ISO-3166-1 alpha-2")
        @Schema(description = "Country code", example = "US")
        String countryCode,

        /** Whether to send automated reminders. */
        @Schema(description = "Send reminders flag", example = "false")
        Boolean sendReminders,

        /** Whether to charge late fees. */
        @Schema(description = "Charge late fees flag", example = "false")
        Boolean chargeLateFees,

        /** Late fee percentage (0.0–100.0). Required if chargeLateFees=true. */
        @DecimalMin(value = "0.0") @DecimalMax(value = "100.0")
        @Schema(description = "Late fee percent", example = "2.5")
        Double lateFeePercent,

        /** Currency code (must be ISO-4217, e.g., USD). */
        @Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be ISO-4217")
        @Schema(description = "Currency code", example = "USD")
        String currencyCode,

        /** Language (e.g., "en" or "en-US"). */
        @Pattern(regexp = "^[A-Za-z]{2,8}(-[A-Za-z0-9]{2,8})?$", message = "language must look like 'en' or 'en-US'")
        @Schema(description = "Language", example = "en")
        String language,

        /** Whether invoice attachments are allowed. */
        @Schema(description = "Allow invoice attachments", example = "false")
        Boolean allowInvoiceAttachments,

        /** Whether the client is active. */
        @Schema(description = "Is active", example = "true")
        Boolean isActive,

        /** Timestamp when created (system-managed, response only). */
        @Schema(description = "Record creation timestamp", example = "2025-08-28T11:44:28Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime createdAt,

        /** Timestamp when last updated (system-managed, response only). */
        @Schema(description = "Record last updated timestamp", example = "2025-08-29T15:22:10Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime updatedAt
) {
    /**
     * Validation method to enforce identity rules:
     * Either companyName OR (firstName + lastName).
     */
    @AssertTrue(message = "Provide either companyName or both firstName and lastName")
    @Schema(hidden = true)
    public boolean identityOk() {
        boolean company = companyName != null && !companyName.isBlank();
        boolean person = (firstName != null && !firstName.isBlank())
                && (lastName != null && !lastName.isBlank());
        return company || person;
    }

    /**
     * Validation method to enforce late fee rules:
     * If chargeLateFees=true, lateFeePercent must be > 0.
     */
    @AssertTrue(message = "lateFeePercent must be > 0 when chargeLateFees=true")
    @Schema(hidden = true)
    public boolean lateFeeOk() {
        return !Boolean.TRUE.equals(chargeLateFees) || (lateFeePercent != null && lateFeePercent > 0);
    }
}
