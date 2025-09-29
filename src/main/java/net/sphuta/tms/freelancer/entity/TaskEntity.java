package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.OffsetDateTime;

/**
 * Entity representing a Task assigned to a Project.
 * Maps to the "tasks" table in the database.
 * Includes fields for task ID, project ID, task name, description,
 * created timestamp, and updated timestamp.
 * Uses Lombok to generate boilerplate code.
 * Timestamps are automatically managed by Hibernate.
 * Indexes the project_id column for faster lookups.
 * Project ID is a foreign key to the projects table.
 * Task name is required and cannot be null.
 * Description is optional and can be null.
 * ID is auto-generated.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_project", columnList = "project_id")
})
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // foreign key to projects table
    @Column(name = "project_id", nullable = false,insertable = false, updatable = false)
    private int projectId;

    @Column(name = "task_name", nullable = false, length = 255)
    private String taskName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private OffsetDateTime createdDt;

    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private OffsetDateTime updatedDt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectEntity project;


}
