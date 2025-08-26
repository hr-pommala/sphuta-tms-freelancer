package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity class representing an Invoice.
 *
 * Features:
 * - Stores client reference, issue/due dates, currency, and notes.
 * - Tracks status (Draft, Sent, Paid, Cancelled).
 * - Maintains creation and update timestamps (auto-managed).
 * - Provides logging on persistence lifecycle events.
 */
@Getter @Setter
@Entity
@Table(
        name = "invoices",
        indexes = @Index(name="idx_inv_client", columnList = "client_id")
)
@Builder @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class Invoice {

    /** Unique identifier for the invoice. */
    @Id
    @GeneratedValue
    private UUID id;

    /** Reference to the client associated with this invoice. */
    @Column(name="client_id", nullable=false)
    private UUID clientId;

    /** Date when the invoice was issued. */
    @Column(name="issue_date", nullable=false)
    private LocalDate issueDate;

    /** Due date for the invoice payment. */
    @Column(name="due_date", nullable=false)
    private LocalDate dueDate;

    /** ISO 4217 currency code used in this invoice. */
    @Column(name="currency_code", length=3, nullable=false)
    private String currencyCode;

    /**
     * Status of the invoice.
     * Can be one of: DRAFT, SENT, PAID, CANCELLED.
     */
    @Enumerated(EnumType.STRING)
    @Column(length=20, nullable=false)
    private Status status;

    /** Optional notes or remarks about the invoice. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the invoice was created (auto-managed). */
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp when the invoice was last updated (auto-managed). */
    @UpdateTimestamp
    @Column(name="updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------- ENTITY LIFECYCLE CALLBACKS -----------------------

    /** Log before persisting a new Invoice entity. */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new Invoice for clientId={}, issueDate={}, dueDate={}", clientId, issueDate, dueDate);
    }

    /** Log after persisting a new Invoice entity. */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted Invoice with id={} and status={}", id, status);
    }

    /** Log before updating an existing Invoice entity. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update Invoice id={}, currentStatus={}", id, status);
    }

    /** Log after updating an existing Invoice entity. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated Invoice id={} at {}, newStatus={}", id, updatedAt, status);
    }

    /** Log after loading an Invoice entity from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded Invoice id={} for clientId={} with status={}", id, clientId, status);
    }

    // ----------------------- ENUM -----------------------

    /**
     * Invoice status lifecycle:
     * - DRAFT: Invoice created but not yet sent.
     * - SENT:  Invoice sent to client.
     * - PAID:  Invoice fully paid.
     * - CANCELLED: Invoice voided or invalidated.
     */
    public enum Status { DRAFT, SENT, PAID, CANCELLED }
}
