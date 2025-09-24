package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationItem;
import net.sphuta.tms.freelancer.entity.Notification;
import net.sphuta.tms.freelancer.entity.NotificationCategory;
import net.sphuta.tms.freelancer.entity.NotificationStatus;

/**
 * Simple mapper for Notification entity <-> DTOs.
 * Static methods keep it simple to use from services.
 */
public final class NotificationMapper {

    private NotificationMapper() {}

    public static Notification toEntity(NotificationCreateRequest req) {
        NotificationCategory category = NotificationCategory.valueOf(req.category());
        NotificationStatus status = NotificationStatus.valueOf(req.status());

        return Notification.builder()
                .userId(req.userId())
                .category(category)
                .status(status)
                .title(req.title())
                .subtitle(req.subtitle())
                .notificationDescription(req.notificationDescription())
                .actionUrl(req.actionUrl())
                .priority(req.priority() == null ? 0 : req.priority())
                .isRead(false)
                .build();
    }

    public static NotificationItem toDto(Notification n, String ackUrl) {
        return new NotificationItem(
                n.getId(),
                n.getTitle(),
                n.getSubtitle(),
                n.getNotificationDescription(),
                n.getActionUrl(),
                n.getPriority(),
                n.isRead(),
                ackUrl
        );
    }
}
