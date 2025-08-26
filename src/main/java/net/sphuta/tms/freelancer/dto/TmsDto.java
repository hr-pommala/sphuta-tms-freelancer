package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ==========================================================
 * TmsDto
 * ==========================================================
 * <p>
 * Central Data Transfer Objects (DTOs) used across the API.
 * These are Java 17 {@code record}s with Lombok {@code @Builder}
 * support for immutability + concise construction.
 * </p>
 *
 * <h2>Structure:</h2>
 * - ClientRequest / ClientResponse → Clients endpoints
 * - TimeEntrySummary / UninvoicedResponse → Time entries
 * - InvoiceCreateRequest / InvoiceResponse → Invoices
 * - EstimateCreateRequest / EstimateResponse → Estimates
 * - ApiResponse / ApiError → Wrappers for success and error responses
 *
 * <h2>Validations:</h2>
 * - Bean Validation annotations enforce constraints at request parsing time.
 * - Cross-field validations (AssertTrue methods) check complex business rules.
 *
 * <h2>Swagger:</h2>
 * - {@code @Schema} annotations add rich docs + examples to Swagger UI.
 *
 * <h2>Notes:</h2>
 * - {@code @JsonInclude(JsonInclude.Include.NON_NULL)} ensures responses don’t include nulls.
 * - DTOs have no business logic beyond validation. All logs are in service/controller layers.
 */
public final class TmsDto {

    // ==========================================================
    // Clients
    // ==========================================================

    /**
     * Request payload for creating or updating a Client.
     *
     * <p><b>Validations:</b></p>
     * <ul>
     *   <li>Email is required and must be valid</li>
     *   <li>Either {@code companyName} OR both {@code firstName} and {@code lastName} must be provided</li>
     *   <li>If {@code chargeLateFees=true}, then {@code lateFeePercent > 0}</li>
     * </ul>
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "ClientRequest", description = "Client create/update payload (flat JSON)")
    public record ClientRequest(

            /** Billing email (required, unique ideally). */
            @Email
            @NotBlank(message = "email is required")
            @Schema(description = "Primary billing email", example = "billing@acme.com")
            String email,

            /** Company name if business client. */
            @Size(max = 255)
            @Schema(description = "Company name", example = "Acme LLC")
            String companyName,

            /** First name if individual client. */
            @Size(max = 200)
            @Schema(description = "Contact first name", example = "John")
            String firstName,

            /** Last name if individual client. */
            @Size(max = 200)
            @Schema(description = "Contact last name", example = "Doe")
            String lastName,

            /** Mobile phone number (loosely validated E.164). */
            @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "mobilePhone must be E.164-like")
            @Size(max = 50)
            @Schema(description = "Mobile phone", example = "+1-555-0100")
            String mobilePhone,

            /** Business/office phone. */
            @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "businessPhone must be E.164-like")
            @Size(max = 50)
            @Schema(description = "Business phone", example = "+1-555-0199")
            String businessPhone,

            /** Address line 1. */
            @Size(max = 500)
            @Schema(description = "Address line 1", example = "1 Main St")
            String addressLine1,

            /** Address line 2 (apartment/suite). */
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

            /** Postal/ZIP code. */
            @Size(max = 40)
            @Schema(description = "Postal code", example = "78701")
            String postalCode,

            /** Country code (ISO-3166-1 alpha-2). */
            @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode must be ISO-3166-1 alpha-2")
            @Schema(description = "Country code", example = "US")
            String countryCode,

            /** Send reminders to client. */
            @Schema(description = "Send reminders flag", example = "false")
            Boolean sendReminders,

            /** Charge late fees to client. */
            @Schema(description = "Charge late fees flag", example = "false")
            Boolean chargeLateFees,

            /** Late fee percentage (0–100). Required if chargeLateFees=true. */
            @DecimalMin(value="0.0") @DecimalMax(value="100.0")
            @Schema(description = "Late fee percent", example = "2.5")
            Double lateFeePercent,

            /** Currency code (ISO-4217). */
            @Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be ISO-4217")
            @Schema(description = "Currency code", example = "USD")
            String currencyCode,

            /** Language (IETF-like tag). */
            @Pattern(regexp = "^[A-Za-z]{2,8}(-[A-Za-z0-9]{2,8})?$", message = "language must look like 'en' or 'en-US'")
            @Schema(description = "Language", example = "en")
            String language,

            /** Allow invoice attachments flag. */
            @Schema(description = "Allow invoice attachments", example = "false")
            Boolean allowInvoiceAttachments,

            /** Active flag (controls Archived/Active tabs). */
            @Schema(description = "Is active", example = "true")
            Boolean isActive
    ) {
        /** Validation: must provide either companyName OR (firstName + lastName). */
        @AssertTrue(message = "Provide either companyName or both firstName and lastName")
        @Schema(hidden = true)
        public boolean identityOk() {
            boolean company = companyName != null && !companyName.isBlank();
            boolean person = (firstName != null && !firstName.isBlank())
                    && (lastName != null && !lastName.isBlank());
            return company || person;
        }

        /** Validation: if chargeLateFees=true then lateFeePercent > 0. */
        @AssertTrue(message = "lateFeePercent must be > 0 when chargeLateFees=true")
        @Schema(hidden = true)
        public boolean lateFeeOk() {
            return !Boolean.TRUE.equals(chargeLateFees) || (lateFeePercent != null && lateFeePercent > 0);
        }
    }

    /** Response model for Clients. */
    @Builder
    @Schema(name = "ClientResponse", description = "Client read model")
    public record ClientResponse(
            UUID id, String companyName, String firstName, String lastName,
            String email, String mobilePhone, String businessPhone,
            String addressLine1, String addressLine2, String city, String state,
            String postalCode, String countryCode, Boolean sendReminders,
            Boolean chargeLateFees, Double lateFeePercent, String currencyCode,
            String language, Boolean allowInvoiceAttachments, Boolean isActive,
            OffsetDateTime createdAt, OffsetDateTime updatedAt
    ) {}

    // ==========================================================
    // Time Entries
    // ==========================================================

    /** Lightweight summary view of a time entry. */
    @Builder
    public record TimeEntrySummary(UUID id, UUID clientId, LocalDate entryDate, BigDecimal hours, String status) {}

    /** Response for GET /time-entries/uninvoiced. */
    @Builder
    public record UninvoicedResponse(UUID clientId, LocalDate from, LocalDate to, List<TimeEntrySummary> entries) {}

    // ==========================================================
    // Invoices
    // ==========================================================

    /** Request payload to create an invoice from time entries. */
    @Builder
    public record InvoiceCreateRequest(
            @NotNull UUID clientId,
            @NotNull LocalDate issueDate,
            @NotNull LocalDate dueDate,
            @Pattern(regexp="^[A-Z]{3}$") String currencyCode,
            @Size(max=1000) String notes,
            @NotEmpty List<UUID> timeEntryIds
    ) {}


    /** Invoice response model. */
    @Builder
    public record InvoiceResponse(
            UUID id, UUID clientId, LocalDate issueDate, LocalDate dueDate,
            String currencyCode, String status, String notes,
            OffsetDateTime createdAt, OffsetDateTime updatedAt
    ) {}

    // ==========================================================
    // Estimates
    // ==========================================================

    /** Single line item within an Estimate. */
    @Builder
    public record EstimateItemRequest(
            @NotBlank String description,
            @NotNull @DecimalMin("0.01") BigDecimal quantity,
            @NotNull @DecimalMin("0.00") BigDecimal unitPrice
    ) {}

    /** Request payload to create a new Estimate. */
    @Builder
    public record EstimateCreateRequest(
            @NotNull UUID clientId,
            @NotNull LocalDate issueDate,
            @NotNull LocalDate validUntil,
            @Pattern(regexp="^[A-Z]{3}$") String currencyCode,
            @Size(max=1000) String notes,
            @NotEmpty List<@Valid EstimateItemRequest> items
    ) {}

    /** Estimate response model. */
    @Builder
    public record EstimateResponse(
            UUID id, UUID clientId, LocalDate issueDate, LocalDate validUntil,
            String currencyCode, String notes, List<EstimateItemRequest> items,
            OffsetDateTime createdAt, OffsetDateTime updatedAt
    ) {}

    // ==========================================================
    // Wrappers
    // ==========================================================

    /** Standard success wrapper with optional pagination. */
    @Builder
    public record ApiResponse<T>(String message, T data, Meta meta) {
        @Builder
        public record Meta(Long total, Integer page, Integer size) { }
    }

    /** Standard error payload aligned with HTTP status codes. */
    @Builder
    public record ApiError(
            String error, String message, String path, Integer status, OffsetDateTime timestamp) {}
}
