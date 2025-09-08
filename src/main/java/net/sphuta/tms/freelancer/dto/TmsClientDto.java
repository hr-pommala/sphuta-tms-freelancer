package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsClientDto", description = "Client unified DTO for requests and responses")
public record TmsClientDto(

        @Schema(description = "Client ID (auto-incremented)", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        String name,

        @Email
        @NotBlank(message = "email is required")
        @Schema(description = "Primary billing email", example = "billing@acme.com")
        String email,

        @Size(max = 255)
        @Schema(description = "Company name", example = "Acme LLC")
        String companyName,

        @Size(max = 200)
        @Schema(description = "Contact first name", example = "John")
        String firstName,

        @Size(max = 200)
        @Schema(description = "Contact last name", example = "Doe")
        String lastName,

        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "mobilePhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Mobile phone", example = "+1-555-0100")
        String mobilePhone,

        @Pattern(regexp = "^[+]?([0-9 ()-]{6,20})$", message = "businessPhone must be E.164-like")
        @Size(max = 50)
        @Schema(description = "Business phone", example = "+1-555-0199")
        String businessPhone,

        @Size(max = 500)
        @Schema(description = "Address line 1", example = "1 Main St")
        String addressLine1,

        @Size(max = 500)
        @Schema(description = "Address line 2", example = "Suite 200")
        String addressLine2,

        @Size(max = 120)
        @Schema(description = "City", example = "Austin")
        String city,

        @Size(max = 120)
        @Schema(description = "State", example = "TX")
        String state,

        @Size(max = 40)
        @Schema(description = "Postal code", example = "78701")
        String postalCode,

        @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode must be ISO-3166-1 alpha-2")
        @Schema(description = "Country code", example = "US")
        String countryCode,

        @Schema(description = "Send reminders flag", example = "false")
        Boolean sendReminders,

        @Schema(description = "Charge late fees flag", example = "false")
        Boolean chargeLateFees,

        @DecimalMin(value = "0.0") @DecimalMax(value = "100.0")
        @Schema(description = "Late fee percent", example = "2.5")
        Double lateFeePercent,

        @Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be ISO-4217")
        @Schema(description = "Currency code", example = "USD")
        String currencyCode,

        @Pattern(regexp = "^[A-Za-z]{2,8}(-[A-Za-z0-9]{2,8})?$", message = "language must look like 'en' or 'en-US'")
        @Schema(description = "Language", example = "en")
        String language,

        @Schema(description = "Allow invoice attachments", example = "false")
        Boolean allowInvoiceAttachments,

        @Schema(description = "Is active", example = "true")
        Boolean isActive,

        @Schema(description = "Record creation timestamp", example = "2025-08-28T11:44:28Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime createdAt,

        @Schema(description = "Record last updated timestamp", example = "2025-08-29T15:22:10Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime updatedAt
) {

        @AssertTrue(message = "Provide either companyName or both firstName and lastName")
        @Schema(hidden = true)
        public boolean identityOk() {
                boolean company = companyName != null && !companyName.isBlank();
                boolean person = (firstName != null && !firstName.isBlank())
                        && (lastName != null && !lastName.isBlank());
                return company || person;
        }

        @AssertTrue(message = "lateFeePercent must be > 0 when chargeLateFees=true")
        @Schema(hidden = true)
        public boolean lateFeeOk() {
                return !Boolean.TRUE.equals(chargeLateFees) || (lateFeePercent != null && lateFeePercent > 0);
        }

        /**
         * Slim factory method for dropdowns and lightweight mappings.
         * Creates a minimal DTO with only id + companyName filled.
         */
        public static TmsClientDto slim(Integer id, String name) {
                return new TmsClientDto(
                        id,
                        name,
                        null,              // email
                        null,       // use displayName as companyName
                        null, null,        // firstName, lastName
                        null, null,        // phones
                        null, null,        // addresses
                        null, null,        // city, state
                        null, null,        // postal, country
                        null, null,        // reminders, late fees
                        null,              // lateFeePercent
                        null, null,        // currency, language
                        null,              // allowInvoiceAttachments
                        null,              // isActive
                        null,              // createdAt
                        null               // updatedAt
                );
        }


}