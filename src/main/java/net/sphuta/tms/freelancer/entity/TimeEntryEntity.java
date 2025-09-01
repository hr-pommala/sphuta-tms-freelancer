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

/**
 * Entity class representing a single Time Entry in the system.
 *
 * <p>This class maps to the {@code time_entries} table in the database.
 * Each TimeEntry represents a row of work hours logged against a {@link TimesheetEntity}.
 *
 * <p>Includes JPA lifecycle callbacks, auditing (created/updated timestamps),
 * and logging at various entity events for better traceability.
 */
@Slf4j
@Entity
@Table(
        name = "time_entries",
        indexes = @Index(name = "idx_time_entries_date", columnList = "entry_date")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryEntity {

    /* ------------------------------- Identifier ------------------------------- */

    /**
     * Primary key identifier for a time entry.
     * Generated automatically by the database using identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /* ------------------------------- Relations -------------------------------- */

    /**
     * Reference to the parent {@link TimesheetEntity}.
     * <p>
     * This is a Many-to-One relationship since multiple time entries
     * can belong to a single timesheet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private TimesheetEntity timesheet;

    /* --------------------------------- Data ----------------------------------- */

    /** The specific calendar date for which the time entry is recorded. */
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Optional description of the work performed (task/notes). */
    private String description;

    /** Hours worked, stored with precision up to 2 decimal places. */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal hours;

    /** Optional start time of the work logged. */
    @Column(name = "start_time")
    private OffsetTime startTime;

    /** Optional end time of the work logged. */
    @Column(name = "end_time")
    private OffsetTime endTime;

    /** Hourly rate applicable at the time of logging. */
    @Column(name = "rate_at_entry", precision = 10, scale = 2)
    private BigDecimal rateAtEntry;

    /** Calculated total cost for this entry (hours × rate). */
    @Column(name = "cost_at_entry", precision = 12, scale = 2)
    private BigDecimal costAtEntry;

    /* --------------------------------- Audit ---------------------------------- */

    /** Timestamp when the record was created. Automatically populated. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp when the record was last updated. Automatically populated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /* ------------------------------- Lifecycle -------------------------------- */

    /**
     * JPA lifecycle callback executed before persisting a new entity.
     * Logs detailed debug information about the entity being saved.
     */
    @PrePersist
    private void onPrePersist() {
        if (log.isDebugEnabled()) {
            log.debug("PrePersist TimeEntry: id={}, timesheetId={}, entryDate={}, hours={}, rateAtEntry={}, startTime={}, endTime={}",
                    id,
                    timesheet != null ? timesheet.getId() : null,
                    entryDate,
                    hours,
                    rateAtEntry,
                    startTime,
                    endTime
            );
        }
    }

    /**
     * JPA lifecycle callback executed after the entity has been persisted.
     * Logs information-level details including calculated cost.
     */
    @PostPersist
    private void onPostPersist() {
        if (log.isInfoEnabled()) {
            log.info("Persisted TimeEntry: id={}, timesheetId={}, entryDate={}, hours={}, costAtEntry={}",
                    id,
                    timesheet != null ? timesheet.getId() : null,
                    entryDate,
                    hours,
                    costAtEntry
            );
        }
    }

    /**
     * JPA lifecycle callback executed before updating an existing entity.
     * Logs debug details about the upcoming changes.
     */
    @PreUpdate
    private void onPreUpdate() {
        if (log.isDebugEnabled()) {
            log.debug("PreUpdate TimeEntry: id={}, entryDate={}, hours={}, rateAtEntry={}, startTime={}, endTime={}",
                    id, entryDate, hours, rateAtEntry, startTime, endTime);
        }
    }

    /**
     * JPA lifecycle callback executed after loading the entity from the database.
     * Logs trace-level details for fine-grained debugging.
     */
    @PostLoad
    private void onPostLoad() {
        if (log.isTraceEnabled()) {
            log.trace("Loaded TimeEntry: id={}, entryDate={}, hours={}", id, entryDate, hours);
        }
    }
}
