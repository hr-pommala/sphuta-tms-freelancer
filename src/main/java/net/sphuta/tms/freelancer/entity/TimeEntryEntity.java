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
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_timeentry_timesheet_date_task_project",
                        columnNames = {"timesheet_id", "entry_date", "task_id", "project_id"}
                )
        },
        indexes = {
//                @Index(name = "idx_te_client", columnList = "client_id"),
//                @Index(name = "idx_te_invoice", columnList = "invoice_id"),
                @Index(name = "idx_te_date", columnList = "entry_date")
        }
)

public class TimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // NEW: project_id (denormalized for faster queries / UI needs)
    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private TimesheetEntity timesheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskEntity task;


    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    @Column(name = "start_time")
    private OffsetTime startTime;

    @Column(name = "end_time")
    private OffsetTime endTime;

    @Column(name = "rate_at_entry", precision = 10, scale = 2)
    private BigDecimal rateAtEntry;


    @Column(name = "cost_at_entry", precision = 12, scale = 2)
    private BigDecimal costAtEntry;

    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private OffsetDateTime createdDt;

    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private OffsetDateTime updatedDt;

//    @Deprecated
//    @Column(name = "client_id")
//    private Integer clientId;
//
//    @Deprecated
//    @Column(name = "invoice_id")
//    private Integer invoiceId;


    // ----------------------------
    // Enum for Workflow Status
    // ----------------------------
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private ProjectEntity project;


}
