package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * ==========================================================
 * TmsTimeEntryEntity
 * ==========================================================
 *
 * JPA Entity representing the `time_entries` table.
 *
 * Purpose:
 * - Stores logged hours of work for a given client and date.
 * - Supports workflow status (PENDING, APPROVED, REJECTED).
 * - May optionally be linked to an invoice (via {@code invoiceId}).
 *
 * Notes:
 * - AUTO_INCREMENT primary key (id).
 * - Indexed by {@code client_id}, {@code invoice_id}, and {@code entry_date}
 *   for optimized search/filtering.
 * - Lifecycle events log persistence operations for traceability.
 */
@Getter
@Setter
@Entity
@Table(
        name = "time_entries",
        indexes = {
                @Index(name = "idx_te_client", columnList = "client_id"),
                @Index(name = "idx_te_invoice", columnList = "invoice_id"),
                @Index(name = "idx_te_date", columnList = "entry_date")
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TimeEntryEntity {

    /** Primary key (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // was UUID

    /** Foreign key reference to the client. */
    @Column(name = "client_id", nullable = false)
    private Integer clientId; // was UUID

    /** Date when the work was performed. */
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Number of hours worked (supports decimals, e.g., 3.50). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    /** Workflow status (PENDING, APPROVED, REJECTED). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    /** Optional reference to an invoice (nullable). */
    @Column(name = "invoice_id")
    private Integer invoiceId; // was UUID (nullable)

    /** Timestamp when the entry was created (system-managed). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp when the entry was last updated (system-managed). */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------------
    // Entity Lifecycle Callbacks
    // ----------------------------

    /** Fires before inserting a new record. */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new TimeEntry for clientId={}, entryDate={}, hours={}",
                clientId, entryDate, hours);
    }

    /** Fires after inserting a new record. */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted TimeEntry with id={} and status={}", id, status);
    }

    /** Fires before updating a record. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update TimeEntry id={}, currentStatus={}, invoiceId={}", id, status, invoiceId);
    }

    /** Fires after updating a record. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated TimeEntry id={} at {}, newStatus={}, invoiceId={}", id, updatedAt, status, invoiceId);
    }

    /** Fires when an entity is loaded from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded TimeEntry id={} for clientId={} with status={} and hours={}",
                id, clientId, status, hours);
    }

    /**
     * Workflow status for time entries.
     * - PENDING  → Entry awaiting approval.
     * - APPROVED → Entry validated and can be invoiced.
     * - REJECTED → Entry denied and excluded from invoicing.
     */
    public enum Status { PENDING, APPROVED, REJECTED }
}
