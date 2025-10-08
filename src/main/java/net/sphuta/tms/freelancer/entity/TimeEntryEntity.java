package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "time_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_timeentry_timesheet_date_task_project",
                        columnNames = {"timesheet_id", "entry_date", "task_id", "project_id"}
                )
        }
)

public class TimeEntryEntity {

    // Auto-generated primary key.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // NEW: project_id (denormalized for faster queries / UI needs)
    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    // Many-to-one relationship to TimesheetEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private TimesheetEntity timesheet;

    // Many-to-one relationship to TaskEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    // Date of the time entry, cannot be null
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    // Description of the time entry, cannot be null
    private String description;

    // Number of hours worked, cannot be null, with 2 decimal places
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    // Start and end times (optional)
    @Column(name = "start_time")
    private OffsetTime startTime;

    // End time (optional)
    @Column(name = "end_time")
    private OffsetTime endTime;

    // Status of the time entry in the workflow
    @Column(name = "rate_at_entry", precision = 10, scale = 2)
    private BigDecimal rateAtEntry;

    // Cost at entry (hourly rate * hours)
    @Column(name = "cost_at_entry", precision = 12, scale = 2)
    private BigDecimal costAtEntry;

    // Workflow status of the time entry
    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private OffsetDateTime createdDt;

    // Timestamp when the task was last updated, auto-set on update
    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private OffsetDateTime updatedDt;


//    // ----------------------------
//    // Enum for Workflow Status
//    // ----------------------------
//    public enum Status {
//        PENDING, APPROVED, REJECTED
//    }
}
