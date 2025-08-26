package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity class representing a Time Entry.
 *
 * Features:
 * - Tracks client, entry date, worked hours, and approval status.
 * - Links to an invoice (if approved and invoiced).
 * - Maintains creation and update timestamps (auto-managed).
 * - Provides logging on persistence lifecycle events.
 */
@Getter @Setter
@Entity
@Table(
        name = "time_entries",
        indexes = {
                @Index(name = "idx_te_client", columnList = "client_id"),
                @Index(name = "idx_te_invoice", columnList = "invoice_id"),
                @Index(name = "idx_te_date", columnList = "entry_date")
        }
)
@Builder @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class TimeEntry {

    /** Unique identifier for the time entry. */
    @Id
    @GeneratedValue
    private UUID id;

    /** Reference to the client associated with this time entry. */
    @Column(name="client_id", nullable=false)
    private UUID clientId;

    /** Date of the work performed. */
    @Column(name="entry_date", nullable=false)
    private LocalDate entryDate;

    /** Number of hours worked (supports decimals). */
    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal hours;

    /**
     * Current status of the time entry.
     * Possible values: PENDING, APPROVED, REJECTED.
     */
    @Enumerated(EnumType.STRING)
    @Column(length=20, nullable=false)
    private Status status;

    /**
     * Identifier of the invoice this entry is linked to (nullable).
     * - Null until invoiced.
     * - Set when the entry is attached to a created invoice.
     */
    @Column(name="invoice_id")
    private UUID invoiceId;

    /** Timestamp when the time entry was created (auto-managed). */
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp when the time entry was last updated (auto-managed). */
    @UpdateTimestamp
    @Column(name="updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------- ENTITY LIFECYCLE CALLBACKS -----------------------

    /** Log before persisting a new TimeEntry entity. */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new TimeEntry for clientId={}, entryDate={}, hours={}", clientId, entryDate, hours);
    }

    /** Log after persisting a new TimeEntry entity. */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted TimeEntry with id={} and status={}", id, status);
    }

    /** Log before updating an existing TimeEntry entity. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update TimeEntry id={}, currentStatus={}, invoiceId={}", id, status, invoiceId);
    }

    /** Log after updating an existing TimeEntry entity. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated TimeEntry id={} at {}, newStatus={}, invoiceId={}", id, updatedAt, status, invoiceId);
    }

    /** Log after loading a TimeEntry entity from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded TimeEntry id={} for clientId={} with status={} and hours={}", id, clientId, status, hours);
    }

    // ----------------------- ENUM -----------------------

    /**
     * Status of the time entry:
     * - PENDING: Entry submitted, awaiting approval.
     * - APPROVED: Entry reviewed and approved.
     * - REJECTED: Entry denied or invalid.
     */
    public enum Status { PENDING, APPROVED, REJECTED }
}
