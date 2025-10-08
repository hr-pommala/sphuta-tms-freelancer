package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ==========================================================
 * {@code ClientEntity}
 * ==========================================================
 *
 * <p>JPA entity mapped to the {@code clients} table in the database.</p>
 *
 * <p><b>Responsibilities:</b></p>
 * - Persist client details (personal, contact, preferences, audit). <br>
 * - Auto-manage audit timestamps via lifecycle hooks {@link #onCreate()} and {@link #onUpdate()}. <br>
 * - Auto-generate display name from {@code firstName + lastName}. <br>
 * - Define one-to-many relationship with {@link ProjectEntity}. <br>
 *
 * <p><b>Design Characteristics:</b></p>
 * - Uses JPA annotations for ORM mapping. <br>
 * - Uses Lombok to generate boilerplate code (getters, setters, constructors, builder). <br>
 * - Implements lifecycle hooks with {@code @PrePersist} and {@code @PreUpdate}. <br>
 * - Domain-driven → entity encapsulates behavior (`generateDisplayName`). <br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clients")
public class ClientEntity {

    // ------------------------------------------------------------------------
    // PRIMARY KEY
    // ------------------------------------------------------------------------

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ------------------------------------------------------------------------
    // BASIC INFO
    // ------------------------------------------------------------------------

    /** Owner user id FK -> users.id (application enforces FK on DB). */
    @Column(name = "user_id", nullable = false)
    private Integer userId;


    /** Display name (auto-generated from firstName + lastName). */
    @Column(nullable = false, length = 255)
    private String name;

    /** Unique email address for client. */
    @Column(nullable = false, length = 255)
    private String email;

    /** Company name (if applicable). */
    @Column(length = 255)
    private String companyName;

    /** Contact’s first name. */
    @Column(length = 200)
    private String firstName;

    /** Contact’s last name. */
    @Column(length = 200)
    private String lastName;

    // ------------------------------------------------------------------------
    // CONTACT DETAILS
    // ------------------------------------------------------------------------

    /** Mobile phone (E.164-like format). */
    @Column(length = 50)
    private String mobilePhone;

    /** Business phone (E.164-like format). */
    @Column(length = 50)
    private String businessPhone;

    // ------------------------------------------------------------------------
    // ADDRESS DETAILS
    // ------------------------------------------------------------------------

    /** Address line 1. */
    @Column(length = 500)
    private String addressLine1;

    /** Address line 2 (optional). */
    @Column(length = 500)
    private String addressLine2;

    /** City. */
    @Column(length = 120)
    private String city;

    /** State/Province. */
    @Column(length = 120)
    private String state;

    /** Postal code. */
    @Column(length = 40)
    private String postalCode;

    /** ISO-3166-1 alpha-2 country code (e.g., "US"). */
    @Column(length = 2)
    private String countryCode;

    // ------------------------------------------------------------------------
    // PREFERENCES
    // ------------------------------------------------------------------------

    /** Whether reminders should be sent to client. */
    @Column(nullable = false)
    private Boolean sendReminders;

    /** Whether late fees should be charged. */
    @Column(nullable = false)
    private Boolean chargeLateFees;

    /** Late fee percentage (0.0 to 100.0). */
    @Column
    private Double lateFeePercent;

    /** ISO-4217 currency code (e.g., "USD"). */
    @Column(length = 3)
    private String currencyCode;

    /** Preferred language (e.g., "en", "en-US"). */
    @Column(length = 20)
    private String language;

    /** Whether invoice attachments are allowed. */
    @Column(nullable = false)
    private Boolean allowInvoiceAttachments;

    /** Client’s active status. */
    @Column(nullable = false)
    private Boolean isActive;

    // ------------------------------------------------------------------------
    // AUDIT FIELDS
    // ------------------------------------------------------------------------

    /** Record creation timestamp (set only once). */
    @Column(nullable = false, updatable = false)
    private Instant createdDt;

    /** Record last updated timestamp (updated automatically). */
    @Column(nullable = false)
    private Instant updatedDt;

    // ------------------------------------------------------------------------
    // RELATIONSHIPS
    // ------------------------------------------------------------------------

    /**
     * One-to-many relationship with projects.
     * A client can have multiple projects.
     */
    @OneToMany(mappedBy = "clientEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectEntity> projects;

    // ------------------------------------------------------------------------
    // LIFECYCLE HOOKS
    // ------------------------------------------------------------------------

    /**
     * Sets createdAt and updatedAt timestamps before persisting.
     * Also generates display name.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdDt = now;
        this.updatedDt = now;
        generateDisplayName();
    }

    /**
     * Updates the updatedAt timestamp before entity update.
     * Also regenerates display name.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedDt = Instant.now();
        generateDisplayName();
    }

    // ------------------------------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------------------------------

    /**
     * Generates display name based on first and last name.
     * If both are empty, sets {@code name} to null.
     */
    private void generateDisplayName() {
        String f = Optional.ofNullable(firstName).filter(s -> !s.trim().isEmpty()).map(String::trim).orElse("");
        String l = Optional.ofNullable(lastName).filter(s -> !s.trim().isEmpty()).map(String::trim).orElse("");
        if (!f.isEmpty() || !l.isEmpty()) {
            this.name = (f + " " + l).trim();

        } else {
            this.name = null; // no first/last name
        }
    }
}