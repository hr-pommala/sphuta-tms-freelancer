package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.sphuta.tms.freelancer.enam.TimesheetStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Timesheet header entity. */
@Entity
@Table(name = "timesheets",
        uniqueConstraints = @UniqueConstraint(name = "uq_timesheet_project_period",
                columnNames = {"project_id", "period_start", "period_end"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Timesheet {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "project_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID projectId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeEntry> entries = new ArrayList<>();
}
