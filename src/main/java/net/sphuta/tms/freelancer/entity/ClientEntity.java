package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Display name (auto-generated from firstName + lastName). */
    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String companyName;

    @Column(length = 200)
    private String firstName;

    @Column(length = 200)
    private String lastName;

    @Column(length = 50)
    private String mobilePhone;

    @Column(length = 50)
    private String businessPhone;

    @Column(length = 500)
    private String addressLine1;

    @Column(length = 500)
    private String addressLine2;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String state;

    @Column(length = 40)
    private String postalCode;

    @Column(length = 2)
    private String countryCode;

    @Column(nullable = false)
    private Boolean sendReminders;

    @Column(nullable = false)
    private Boolean chargeLateFees;

    @Column
    private Double lateFeePercent;

    @Column(length = 3)
    private String currencyCode;

    @Column(length = 20)
    private String language;

    @Column(nullable = false)
    private Boolean allowInvoiceAttachments;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "clientEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectEntity> projects;

    // ---------------- lifecycle hooks ----------------
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        generateDisplayName();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        generateDisplayName();
    }

    // ---------------- helper method ----------------
    private void generateDisplayName() {
        if ((firstName != null && !firstName.isBlank()) ||
                (lastName != null && !lastName.isBlank())) {

            String f = (firstName != null) ? firstName.trim() : "";
            String l = (lastName != null) ? lastName.trim() : "";
            this.name = (f + " " + l).trim();

        } else {
            this.name = null; // no first/last name
        }
    }
}
