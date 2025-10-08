package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.sphuta.tms.freelancer.enums.NotificationCategory;
import net.sphuta.tms.freelancer.enums.NotificationStatus;

import java.time.OffsetDateTime;

/**
 * Notification entity representing a user notification in the system.
 * Mapped to the "notifications" table in the database.
 * Uses field access for JPA to avoid duplicate mapping issues.
 */

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Notification {

    /** Primary key of the notification, auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the user to whom the notification belongs. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Category of the notification (e.g., INFO, ALERT). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    /** Status of the notification (e.g., NEW, READ). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /** Title of the notification. */
    @Column(nullable = false, columnDefinition = "text")
    private String title;

    /** Subtitle of the notification. */
    @Column(columnDefinition = "text")
    private String subtitle;

    /** Detailed description of the notification. */
    @Column(name = "notification_description", nullable = false, columnDefinition = "text")
    private String notificationDescription;

    /** Optional URL associated with the notification action. */
    @Column(name = "action_url")
    private String actionUrl;

    /** Priority level of the notification, default is 0. */
    @Column(nullable = false)
    private Integer priority = 0;

    /** Flag indicating if the notification has been read, default is false. */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /** Timestamp when the notification was created. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Timestamp when the notification was last updated. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /** Timestamp when the notification was read. */
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Timestamp when the notification was last updated. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
