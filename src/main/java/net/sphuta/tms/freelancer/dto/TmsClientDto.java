package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * ==========================================================
 * {@code TmsClientDto}
 * ==========================================================
 *
 * <p>Unified DTO (Data Transfer Object) for representing a client
 * in both **requests** and **responses**.</p>
 *
 * <p><b>Design Characteristics:</b></p>
 * - Declared as a {@link java.lang.Record} → immutability + auto-generated methods. <br>
 * - Uses **Jakarta Bean Validation** annotations for request payload validation. <br>
 * - Uses **Jackson** annotations for controlling JSON serialization/deserialization. <br>
 * - Uses **Swagger/OpenAPI** annotations for rich API documentation. <br>
 * - Built with Lombok {@link Builder} for convenient construction. <br>
 *
 * <p><b>Usage:</b></p>
 * - Input: request bodies for client creation or update. <br>
 * - Output: API responses with read-only fields like {@code id}, {@code createdAt}, etc. <br>
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsClientDto", description = "Client unified DTO for requests and responses")
public record TmsClientDto(

        // ------------------------------------------------------------------------
        // IDENTIFIER
        // ------------------------------------------------------------------------

        /** Auto-generated primary key for the client (read-only). */
        @Schema(description = "Client ID (auto-incremented)", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        // ------------------------------------------------------------------------
        // CONTACT DETAILS
        // ------------------------------------------------------------------------

        /** Primary billing email (required, validated with @Email). */
        @Email
        @NotBlank(message = "email is required")
        @Schema(description = "Primary billing email", example = "billing@acme.com")
        String email,

        /** Legal or trade name of the company. */
        @Size(max = 255)
        @Schema(description = "Company name", example = "Acme LLC")
        String companyName,

        /** First name of primary contact (write-only, not returned in responses). */
        @Size(max = 200)
        @JsonProperty(access = Access.WRITE_ONLY)
        @Schema(description = "Contact first name", example = "Swathi", accessMode = Schema.AccessMode.WRITE_ONLY)
        String firstName,

        /** Last name of primary contact (write-only, not returned in responses). */
        @Size(max = 200)
        @JsonProperty(access = Access.WRITE_ONLY)
        @Schema(description = "Contact last name", example = "Medisetti", accessMode = Schema.AccessMode.WRITE_ONLY)
        String lastName,

        /** Mobile phone in E.164-like format (e.g., +1-555-0100). */
        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "mobilePhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Mobile phone", example = "+1-555-0100")
        String mobilePhone,

        /** Business phone in E.164-like format (e.g., +1-555-0199). */
        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "businessPhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Business phone", example = "+1-555-0199")
        String businessPhone,

        // ------------------------------------------------------------------------
        // ADDRESS DETAILS
        // ------------------------------------------------------------------------

        /** Primary address line. */
        @Size(max = 500)
        @Schema(description = "Address line 1", example = "1 Main St")
        String addressLine1,

        /** Secondary address line (optional). */
        @Size(max = 500)
        @Schema(description = "Address line 2", example = "Suite 200")
        String addressLine2,

        /** City of client’s location. */
        @Size(max = 120)
        @Schema(description = "City", example = "Austin")
        String city,

        /** State or province. */
        @Size(max = 120)
        @Schema(description = "State", example = "TX")
        String state,

        /** Postal code (ZIP). */
        @Size(max = 40)
        @Schema(description = "Postal code", example = "78701")
        String postalCode,

        /** ISO-3166-1 alpha-2 country code (e.g., "US"). */
        @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode must be ISO-3166-1 alpha-2")
        @Schema(description = "Country code", example = "US")
        String countryCode,

        // ------------------------------------------------------------------------
        // PREFERENCES
        // ------------------------------------------------------------------------

        /** Whether the client should receive reminders. */
        @Schema(description = "Send reminders flag", example = "false")
        Boolean sendReminders,

        /** Whether to charge late fees. */
        @Schema(description = "Charge late fees flag", example = "false")
        Boolean chargeLateFees,

        /** Late fee percentage (0.0 to 100.0). */
        @DecimalMin(value = "0.0") @DecimalMax(value = "100.0")
        @Schema(description = "Late fee percent", example = "2.5")
        Double lateFeePercent,

        /** ISO-4217 currency code (e.g., "USD"). */
        @Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be ISO-4217")
        @Schema(description = "Currency code", example = "USD")
        String currencyCode,

        /** Language code (e.g., "en" or "en-US"). */
        @Pattern(regexp = "^[A-Za-z]{2,8}(-[A-Za-z0-9]{2,8})?$", message = "language must look like 'en' or 'en-US'")
        @Schema(description = "Language", example = "en")
        String language,

        /** Whether invoice attachments are allowed. */
        @Schema(description = "Allow invoice attachments", example = "false")
        Boolean allowInvoiceAttachments,

        /** Active status of client. */
        @Schema(description = "Is active", example = "true")
        Boolean isActive,

        // ------------------------------------------------------------------------
        // AUDIT FIELDS
        // ------------------------------------------------------------------------

        /** Timestamp when record was created (read-only). */
        @Schema(description = "Record creation timestamp", example = "2025-08-28T11:44:28Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime createdAt,

        /** Timestamp when record was last updated (read-only). */
        @Schema(description = "Record last updated timestamp", example = "2025-08-29T15:22:10Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime updatedAt,

        /** Computed display name (read-only, usually first + last name). */
        @Schema(description = "Full display name", example = "Swathi Medisetti", accessMode = Schema.AccessMode.READ_ONLY)
        String name
) {}
