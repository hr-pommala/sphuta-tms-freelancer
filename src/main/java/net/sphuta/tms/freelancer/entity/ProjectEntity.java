package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity class that maps to the {@code projects} table.
 * <p>
 * Represents a project belonging to a client. Each project has metadata such as
 * hourly rate, date range, description, and audit timestamps.
 * Constraints ensure project uniqueness per client.
 */
@Entity
@Table(
        name = "projects",
        uniqueConstraints = {
                // Ensures that a client cannot have multiple projects with the same name.
                @UniqueConstraint(name = "uq_project_per_client", columnNames = {"client_id", "name"})
        },
        indexes = {
                // Index for faster lookups by client ID.
                @Index(name = "ix_projects_client", columnList = "client_id"),
                // Index for faster search by project name.
                @Index(name = "ix_projects_name", columnList = "name")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectEntity {

    /**
     * Primary key of the project.
     * Auto-generated using identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * The client to which this project belongs.
     * Defined as a mandatory {@link ManyToOne} relationship.
     * Fetched lazily to avoid unnecessary loading.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity clientEntity;

    /**
     * Project name (required).
     * Limited to 255 characters.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Optional project code for unique identification.
     * Limited to 100 characters.
     */
    @Column(name = "code", length = 100)
    private String code;

    /**
     * Hourly billing rate for the project.
     * Precision ensures up to 10 digits with 2 decimals.
     * Cannot be null.
     */
    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * The project start date.
     * Nullable if project start is not defined.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The project end date.
     * Nullable if the project is ongoing.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Detailed project description.
     * Stored as {@code TEXT} in database to allow longer content.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Flag indicating if the project is active or archived.
     * <p>
     * Defaults to {@code true} when using {@link Builder}.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Timestamp marking when the record was first created.
     * Automatically populated by Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private Instant createdDt;

    /**
     * Timestamp marking the last time the record was updated.
     * Automatically managed by Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private Instant updatedDt;

    /**
     * Version field used for optimistic locking.
     * Helps prevent concurrent update conflicts.
     */
    @Version
    private long version;
}
