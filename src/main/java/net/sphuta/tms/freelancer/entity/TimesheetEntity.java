package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a Timesheet in the system.
 *
 * <p>This maps to the {@code timesheets} table in the database.
 * A timesheet acts as a container for multiple {@link TimeEntryEntity} records,
 * and stores period-related and project-related information.
 */
@Entity
@Table(
        name = "timesheets",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_timesheet_project_period",
                columnNames = {"project_id", "period_start", "period_end"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetEntity {

    /**
     * Primary key for the timesheet entity.
     * Auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Auto-increment int PK
    private int id;

    /**
     * ID of the project this timesheet belongs to.
     */
    @Column(name = "project_id", insertable = false, updatable = false)
    private int projectId;

    /**
     * Start date of the timesheet period.
     */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /**
     * End date of the timesheet period.
     */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /**
     * Status of the timesheet (e.g., DRAFT, SUBMITTED, APPROVED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status;

    /**
     * Timestamp when the record was created.
     * Automatically set when the record is first persisted.
     */
    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private OffsetDateTime createdDt;

    /**
     * Timestamp when the record was last updated.
     * Automatically refreshed on every update.
     */
    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private OffsetDateTime updatedDt;

    /**
     * One-to-many relationship with time entries.
     *
     * <p>Each timesheet can have multiple {@link TimeEntryEntity} objects.
     * Cascade and orphan removal ensure that when a timesheet is saved or deleted,
     * associated entries are also managed automatically.
     */
    @OneToMany(
            mappedBy = "timesheet",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<TimeEntryEntity> entries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

}
