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
import java.util.UUID;

/**
 * Entity class representing an Estimate for a client.
 *
 * Features:
 * - Stores client information, issue date, validity date, and currency.
 * - Contains embedded line items (products/services).
 * - Tracks creation and update timestamps.
 * - Provides lifecycle logging for persistence events.
 */
@Getter @Setter
@Entity
@Table(
        name="estimates",
        indexes = @Index(name="idx_est_client", columnList="client_id")
)
@Builder @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class Estimate {

    /** Unique identifier for the estimate. */
    @Id
    @GeneratedValue
    private UUID id;

    /** Reference to the client this estimate belongs to. */
    @Column(name="client_id", nullable=false)
    private UUID clientId;

    /** Date when the estimate was issued. */
    @Column(name="issue_date", nullable=false)
    private LocalDate issueDate;

    /** Expiration date of the estimate. */
    @Column(name="valid_until", nullable=false)
    private LocalDate validUntil;

    /** ISO 4217 currency code for this estimate. */
    @Column(name="currency_code", length=3, nullable=false)
    private String currencyCode;

    /** Optional notes or comments about the estimate. */
    @Column(columnDefinition="TEXT")
    private String notes;

    /**
     * Line items belonging to the estimate.
     * Stored as an element collection in a separate table.
     */
    @ElementCollection
    @CollectionTable(
            name="estimate_items",
            joinColumns = @JoinColumn(name="estimate_id")
    )
    private List<Item> items;

    /** Timestamp when the estimate was created (auto-managed). */
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp when the estimate was last updated (auto-managed). */
    @UpdateTimestamp
    @Column(name="updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------- ENTITY LIFECYCLE CALLBACKS -----------------------

    /** Log before persisting a new Estimate entity. */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new Estimate for clientId={}, issueDate={}", clientId, issueDate);
    }

    /** Log after persisting a new Estimate entity. */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted Estimate with id={}", id);
    }

    /** Log before updating an existing Estimate entity. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update Estimate id={}", id);
    }

    /** Log after updating an existing Estimate entity. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated Estimate id={} at {}", id, updatedAt);
    }

    /** Log after loading an Estimate entity from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded Estimate id={} for clientId={}", id, clientId);
    }

    // ----------------------- NESTED CLASS -----------------------

    /**
     * Embedded line item within an Estimate.
     * Stores description, quantity, and unit price.
     */
    @Embeddable
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Item {

        /** Description of the item/service. */
        @Column(nullable=false)
        private String description;

        /** Quantity of the item (supports decimals). */
        @Column(nullable=false, precision=10, scale=2)
        private BigDecimal quantity;

        /** Unit price of the item. */
        @Column(name="unit_price", nullable=false, precision=10, scale=2)
        private BigDecimal unitPrice;
    }
}
