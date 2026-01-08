package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * ==========================================================
 * TmsInvoiceEntity
 * ==========================================================
 *
 * JPA Entity representing the `invoices` table.
 *
 * Purpose:
 * - Stores invoice records linked to a client.
 * - Tracks issue/due dates, currency, status, and optional notes.
 * - Lifecycle hooks log persistence events for observability.
 *
 * Notes:
 * - {@code id} is auto-generated (AUTO_INCREMENT).
 * - {@code status} transitions are controlled via {@link Status}.
 * - Indexed by {@code client_id} for efficient lookups.
 */
@Data
@Entity
@Table(name = "invoices")
@Builder
@AllArgsConstructor
@Slf4j
public class InvoiceEntity {

    /** Primary key (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // was UUID

    /** Foreign key reference to the client who owns the invoice. */
    @Column(name = "client_id", nullable = false)
    private Integer clientId; // was UUID

    /** Date when the invoice was issued. */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** Date when the invoice is due. */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Currency code (ISO-4217, e.g., USD). */
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    /** Current status of the invoice (Enum: DRAFT, SENT, PAID, CANCELLED). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    /** Optional notes or special instructions for the invoice. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the record was first created (system-managed). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp when the record was last updated (system-managed). */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    /**
     * Enumeration for Invoice status lifecycle.
     * Defines allowed transitions: DRAFT → SENT → PAID or CANCELLED.
     */
    public enum Status { DRAFT, SENT, PAID, CANCELLED }
}
