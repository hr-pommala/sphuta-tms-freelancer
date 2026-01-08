package net.sphuta.tms.freelancer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing invoicing settings.
 *
 * <p>This record is used for both API requests and responses, ensuring
 * consistent data representation across layers.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Uses Java 17 {@code record} for immutability and less boilerplate.</li>
 *   <li>Validation annotations ensure data integrity (e.g., @NotBlank, @DecimalMin).</li>
 *   <li>OpenAPI/Swagger {@link Schema} annotations provide API documentation.</li>
 * </ul>
 */
@Schema(description = "Data Transfer Object for Invoicing Settings")
public record SettingsInvoicingDTO(

        /**
         * Unique identifier of the user.
         * Typically a numeric ID.
         */
        @NotNull
        @Schema(description = "Unique identifier for the user", example = "1001")
        int userId,

        /**
         * Currency code in ISO 4217 format.
         * Example: USD, EUR, INR.
         */
        @NotBlank
        @Size(max = 3)
        @Schema(description = "Currency code", example = "USD")
        String currency,

        /**
         * Tax identifier number (optional).
         * Can be a business tax ID, VAT number, etc.
         */
        @Size(max = 64)
        @Schema(description = "Tax identifier number (optional)", example = "12-3456789")
        String taxId,

        /**
         * Default tax rate applied to invoices.
         * Must be between 0.0 and 1.0 (representing 0%–100%).
         */
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true, message = "Default tax rate cannot be negative")
        @DecimalMax(value = "1.0", inclusive = true, message = "Default tax rate must be less than or equal to 1.0")
        @Schema(description = "Default tax rate (e.g., 0.08 = 8%)", example = "0.08")
        BigDecimal defaultTaxRate,

        /**
         * Format for generating invoice numbers.
         * Can include placeholders (e.g., year, sequence).
         */
        @NotBlank
        @Size(max = 64)
        @Schema(description = "Invoice number format", example = "INV-${yyyy}${seq:5}")
        String invoiceNumberFormat,

        /**
         * Number of days allowed for payment.
         * Must be zero or a positive integer.
         */
        @NotNull
        @Min(value = 0, message = "Payment terms must be zero or positive days")
        @Schema(description = "Payment terms in days", example = "14")
        int paymentTermsDays,

        /**
         * Late fee percentage applied after due date.
         * Example: 0.05 = 5% late fee.
         */
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true, message = "Late fee percentage cannot be negative")
        @Schema(description = "Late fee percentage", example = "0.05")
        BigDecimal lateFeePercent,

        /**
         * Identifier of the invoice template.
         * Used to select a predefined format for rendering invoices.
         */
        @NotBlank
        @Size(max = 64)
        @Schema(description = "Template ID for invoice", example = "tmpl_default")
        String templateId,

        /**
         * Optional logo file ID for branding invoices.
         * Typically references a file stored in the system.
         */
        @Size(max = 36)
        @Schema(description = "Logo file identifier (optional)", example = "logo-uuid-1234")
        String logoFileId,

        @Schema(description = "Timestamp when the settings were last updated (set server-side)", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedDt
) {}
