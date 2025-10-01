package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;

public interface NotificationService {

    NotificationListResponse listUnreadNotifications(long userId, int limit, int offset);

    Long createNotification(NotificationCreateRequest req);

    void markOneRead(long notificationId);

    void markAllRead();
}
