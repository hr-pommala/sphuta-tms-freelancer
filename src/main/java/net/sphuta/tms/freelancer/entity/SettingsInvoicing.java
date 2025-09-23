package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the invoicing settings for a user.
 *
 * <p>This entity is mapped to the database table {@code settings_invoicing} and
 * stores configurable details for generating invoices, such as currency, tax rate,
 * invoice format, payment terms, and branding options.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Uses Lombok annotations ({@code @Data}, {@code @Builder}, etc.) to reduce boilerplate.</li>
 *   <li>Includes default values for commonly used fields (e.g., USD as default currency).</li>
 *   <li>Automatically updates {@code updatedAt} timestamp before insert and update operations.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "settings_invoicing")
public class SettingsInvoicing {

    /**
     * Unique identifier for the user.
     * Serves as the primary key in the table.
     */
    @Id
    @Column(name = "user_id")
    private int userId;

    /**
     * Currency code used for invoicing (ISO 4217 format).
     * Default value is "USD".
     */
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    /**
     * Optional tax identifier number, such as VAT or GST ID.
     */
    @Column(name = "tax_id", length = 64)
    private String taxId;

    /**
     * Default tax rate applied to invoices.
     * Precision = 5, Scale = 4 allows values like 0.0800 (8%).
     * Default = 0.0000.
     */
    @Column(name = "default_tax_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal defaultTaxRate = BigDecimal.valueOf(0.0000);

    /**
     * Format string for invoice numbers.
     * Can include placeholders like year and sequence number.
     * Example: "INV-${yyyy}${seq:5}".
     */
    @Column(name = "invoice_number_format", length = 64, nullable = false)
    private String invoiceNumberFormat = "INV-${yyyy}${seq:5}";

    /**
     * Payment terms in days.
     * Default value = 14 (two weeks).
     */
    @Column(name = "payment_terms_days", nullable = false)
    private int paymentTermsDays = 14;

    /**
     * Late fee percentage applied after the due date.
     * Precision = 6, Scale = 3 allows values like 0.050 (5%).
     * Default = 0.000.
     */
    @Column(name = "late_fee_percent", precision = 6, scale = 3, nullable = false)
    private BigDecimal lateFeePercent = BigDecimal.valueOf(0.000);

    /**
     * Template ID for rendering invoice layouts.
     * Default = "tmpl_default".
     */
    @Column(name = "template_id", length = 64, nullable = false)
    private String templateId = "tmpl_default";

    /**
     * Optional file ID for a logo to be displayed on invoices.
     */
    @Column(name = "logo_file_id", length = 36)
    private String logoFileId;

    /**
     * Timestamp representing the last update to this record.
     * Automatically managed by {@link #updateTimestamp()}.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle callback that sets the {@code updatedAt} field
     * automatically before persisting or updating the entity.
     */
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}
