package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;

public interface NotificationService {

    NotificationListResponse listNotifications(long userId, boolean unreadOnly, int limit, int offset);

    /**
     * Create a notification from request DTO. Returns created notification id.
     */
    Long createNotification(NotificationCreateRequest req);

    /**
     * Ack (mark single notification as read) using token t. Returns number of updated rows and unread after update.
     */
    AckResult ackNotification(long userId, String token);

    /**
     * Mark all notifications as read for user, return number updated and unread after update (should be 0).
     */
    AckResult markAllRead(long userId);

    record AckResult(int updated, long unread) {}
}
