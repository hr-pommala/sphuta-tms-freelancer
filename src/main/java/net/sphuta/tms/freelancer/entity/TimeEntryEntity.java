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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "time_entries",
        indexes = {
                @Index(name = "idx_te_client", columnList = "client_id"),
                @Index(name = "idx_te_invoice", columnList = "invoice_id"),
                @Index(name = "idx_te_date", columnList = "entry_date")
        }
)
public class TimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private TimesheetEntity timesheet;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    @Column(name = "start_time")
    private OffsetTime startTime;

    @Column(name = "end_time")
    private OffsetTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    @Column(name = "rate_at_entry", precision = 10, scale = 2)
    private BigDecimal rateAtEntry;

    @Column(name = "invoice_id")
    private Integer invoiceId;

    @Column(name = "cost_at_entry", precision = 12, scale = 2)
    private BigDecimal costAtEntry;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ----------------------------
    // Lifecycle Callbacks
    // ----------------------------

    @PrePersist
    private void beforePersist() {
        log.info("About to persist new TimeEntry for clientId={}, entryDate={}, hours={}", clientId, entryDate, hours);
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

    @PostPersist
    private void afterPersist() {
        log.info("Persisted TimeEntry with id={} and status={}", id, status);
        if (log.isInfoEnabled()) {
            log.info("PostPersist TimeEntry: id={}, timesheetId={}, entryDate={}, hours={}, costAtEntry={}",
                    id,
                    timesheet != null ? timesheet.getId() : null,
                    entryDate,
                    hours,
                    costAtEntry
            );
        }
    }

    @PreUpdate
    private void beforeUpdate() {
        log.info("About to update TimeEntry id={}, currentStatus={}, invoiceId={}", id, status, invoiceId);
        if (log.isDebugEnabled()) {
            log.debug("PreUpdate TimeEntry: id={}, entryDate={}, hours={}, rateAtEntry={}, startTime={}, endTime={}",
                    id, entryDate, hours, rateAtEntry, startTime, endTime);
        }
    }

    @PostUpdate
    private void afterUpdate() {
        log.info("Updated TimeEntry id={} at {}, newStatus={}, invoiceId={}", id, updatedAt, status, invoiceId);
    }

    @PostLoad
    private void afterLoad() {
        log.debug("Loaded TimeEntry id={} for clientId={} with status={} and hours={}", id, clientId, status, hours);
        if (log.isTraceEnabled()) {
            log.trace("PostLoad TimeEntry: id={}, entryDate={}, hours={}", id, entryDate, hours);
        }
    }

    // ----------------------------
    // Enum for Workflow Status
    // ----------------------------
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
