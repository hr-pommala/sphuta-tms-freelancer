package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * ==========================================================
 * TmsEstimateEntity
 * ==========================================================
 *
 * JPA Entity representing the `estimates` table.
 *
 * Purpose:
 * - Stores client estimates, including metadata (issue/valid dates,
 *   currency, notes) and a collection of line items.
 * - Backed by MySQL with AUTO_INCREMENT primary key.
 *
 * Notes:
 * - Each estimate belongs to a client (by {@code clientId}).
 * - Items are stored in a separate table (`estimate_items`) using @ElementCollection.
 * - Lifecycle events (@PrePersist, @PostUpdate, etc.) log important state transitions.
 */
@Getter
@Setter
@Entity
@Table(
        name = "estimates",
        indexes = @Index(name = "idx_est_client", columnList = "client_id")
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EstimateEntity {

    /** Primary key (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // was UUID

    /** Reference to client (foreign key to clients table). */
    @Column(name = "client_id", nullable = false)
    private Integer clientId; // was UUID

    /** Date when the estimate was issued. */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** Date until the estimate is valid. */
    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    /** Currency code (ISO-4217). */
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    /** Optional notes for the estimate (free text). */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Line items included in this estimate (stored in separate table). */
    @ElementCollection
    @CollectionTable(
            name = "estimate_items",
            joinColumns = @JoinColumn(name = "estimate_id")
    )
    private List<Item> items;

    /** Timestamp when the estimate was first created (system-managed). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp when the estimate was last updated (system-managed). */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------------
    // Entity Lifecycle Callbacks
    // ----------------------------

    /** Fires before inserting a new record (logs context). */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new Estimate for clientId={}, issueDate={}", clientId, issueDate);
    }

    /** Fires after inserting a new record (logs assigned ID). */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted Estimate with id={}", id);
    }

    /** Fires before updating a record. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update Estimate id={}", id);
    }

    /** Fires after updating a record. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated Estimate id={} at {}", id, updatedAt);
    }

    /** Fires when an entity is loaded from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded Estimate id={} for clientId={}", id, clientId);
    }

    /**
     * ==========================================================
     * Embedded Item class
     * ==========================================================
     *
     * Represents a line item inside an estimate.
     * Stored in `estimate_items` table via @ElementCollection.
     */
    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /** Description of the line item. */
        @Column(nullable = false)
        private String description;

        /** Quantity of the line item (with precision). */
        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal quantity;

        /** Unit price of the line item. */
        @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
        private BigDecimal unitPrice;
    }
}
