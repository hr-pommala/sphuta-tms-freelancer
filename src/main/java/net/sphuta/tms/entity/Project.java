package net.sphuta.tms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a Project.
 *
 * <p>This entity is mapped directly to the {@code projects} table and reflects
 * the schema constraints defined in your DDL:</p>
 *
 * <ul>
 *   <li>Each project belongs to exactly one {@link Client} (via {@code client_id}).</li>
 *   <li>Unique constraint across (client_id, name) to prevent duplicate names per client.</li>
 *   <li>Optional {@code code}, unique per client if provided.</li>
 *   <li>Hourly rate must be > 0; precision set to 2 decimals.</li>
 *   <li>Check constraint ensures endDate ≥ startDate (validated at DB or service layer).</li>
 * </ul>
 *
 * <p>Additional features:</p>
 * <ul>
 *   <li>Auditing via {@link CreationTimestamp} and {@link UpdateTimestamp}.</li>
 *   <li>Optimistic locking with {@link Version} to prevent concurrent overwrite.</li>
 * </ul>
 */
@Entity
@Table(
        name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_project_per_client", columnNames = {"client_id", "name"})
        },
        indexes = {
                @Index(name="ix_projects_client", columnList = "client_id"),
                @Index(name="ix_projects_name", columnList = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    /**
     * Primary key for the project.
     * <p>Auto-generated UUID using JPA/Hibernate strategy.</p>
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * The owning client of this project.
     * <p>Mandatory many-to-one association. Lazy loaded to avoid N+1 queries
     * unless explicitly fetched in repository methods.</p>
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Project name. Required, unique within a client.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Optional short code for the project.
     * <p>Unique per client if provided.</p>
     */
    @Column(name = "code", length = 100)
    private String code;

    /**
     * Hourly billing rate.
     * <p>Not nullable, precision 10 with 2 decimals.</p>
     */
    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * Optional start date of the project.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Optional end date of the project.
     * <p>Must be greater than or equal to start date if both are present.</p>
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Optional free-form description of the project.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Active/archived flag.
     * <p>True if active; false if archived.</p>
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Timestamp automatically set when the project is first persisted.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp automatically updated whenever the project is modified.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Optimistic locking version.
     * <p>Prevents lost updates in concurrent transactions.</p>
     */
    @Version
    private long version;

    /**
     * Custom toString for debugging/logging without triggering lazy client loading.
     * Only prints project id, name, and code.
     */
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", active=" + active +
                '}';
    }
}
