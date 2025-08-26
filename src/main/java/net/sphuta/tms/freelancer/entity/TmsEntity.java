package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity class representing a Client in the TMS system.
 *
 * Features:
 * - Stores client/company details (personal, contact, address).
 * - Includes preferences for reminders, late fees, and invoice attachments.
 * - Tracks active status, creation time, and update time.
 * - Provides logging on persistence lifecycle events.
 */
@Getter @Setter
@Entity
@Table(
        name = "clients",
        indexes = {
                @Index(name = "idx_clients_email", columnList = "email"),
                @Index(name = "idx_clients_active", columnList = "is_active")
        }
)
@Builder @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class TmsEntity {

    /** Unique identifier for the client. */
    @Id
    @GeneratedValue
    private UUID id;

    /** Registered company name of the client. */
    @Column(name = "company_name", length = 255)
    private String companyName;

    /** First name of the client’s contact person. */
    @Column(name = "first_name", length = 200)
    private String firstName;

    /** Last name of the client’s contact person. */
    @Column(name = "last_name", length = 200)
    private String lastName;

    /** Primary email address of the client (must be unique). */
    @Column(nullable = false, length = 320)
    private String email;

    /** Mobile phone number of the client. */
    @Column(name = "mobile_phone", length = 50)
    private String mobilePhone;

    /** Business/office phone number of the client. */
    @Column(name = "business_phone", length = 50)
    private String businessPhone;

    /** Address line 1 (street, house no., etc.). */
    @Column(name = "address_line1")
    private String addressLine1;

    /** Address line 2 (optional details like apartment, suite, etc.). */
    @Column(name = "address_line2")
    private String addressLine2;

    /** City where the client is located. */
    @Column(length = 120)
    private String city;

    /** State/region of the client’s address. */
    @Column(length = 120)
    private String state;

    /** Postal/ZIP code of the client. */
    @Column(name = "postal_code", length = 40)
    private String postalCode;

    /** Country code in ISO Alpha-2 format (e.g., IN, US). */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /** Flag to indicate whether reminders should be sent to this client. */
    @Column(name = "send_reminders", nullable = false)
    private boolean sendReminders;

    /** Flag to indicate whether late fees should be charged to this client. */
    @Column(name = "charge_late_fees", nullable = false)
    private boolean chargeLateFees;

    /** Percentage of late fees applied (if enabled). */
    @Column(name = "late_fee_percent")
    private Double lateFeePercent;

    /** Preferred currency code (ISO 4217). */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /** Preferred language code (e.g., EN, FR, DE). */
    @Column(length = 10)
    private String language;

    /** Flag to indicate whether invoice attachments are allowed. */
    @Column(name = "allow_invoice_attachments", nullable = false)
    private boolean allowInvoiceAttachments;

    /** Active flag (defaults to true when using builder). */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /** Timestamp when the client was created (auto-managed). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp when the client was last updated (auto-managed). */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ----------------------- ENTITY LIFECYCLE CALLBACKS -----------------------

    /** Log before persisting a new Client entity. */
    @PrePersist
    private void beforePersist() {
        log.info("About to persist new Client with email={} and companyName={}", email, companyName);
    }

    /** Log after persisting a new Client entity. */
    @PostPersist
    private void afterPersist() {
        log.info("Persisted Client with id={} and email={}", id, email);
    }

    /** Log before updating an existing Client entity. */
    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update Client id={} (active={})", id, isActive);
    }

    /** Log after updating an existing Client entity. */
    @PostUpdate
    private void afterUpdate() {
        log.info("Updated Client id={} at {}", id, updatedAt);
    }

    /** Log after loading a Client entity from the database. */
    @PostLoad
    private void afterLoad() {
        log.debug("Loaded Client id={} email={} active={}", id, email, isActive);
    }
}
