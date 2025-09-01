package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * ==========================================================
 * ClientEntity
 * ==========================================================
 *
 * Represents a client in the Freelancer TMS system.
 * Stores both company and individual client details.
 */
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

    /** Display name (derived or provided). Used for listings and sorting. */
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
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Relationship to projects owned by this client.
     * (Assuming you have a ProjectEntity with a `clientEntity` field.)
     */
    @OneToMany(mappedBy = "clientEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectEntity> projects;
}
