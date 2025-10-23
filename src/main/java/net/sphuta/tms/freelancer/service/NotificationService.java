package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
/**
 * Service interface for managing user notifications.
 * Provides methods to list, create, and mark notifications as read.
 * All methods are designed to be transactional.
 */
public interface NotificationService {

    /** List unread notifications for a user with pagination.
     * @param userId ID of the user
     * @param limit Maximum number of notifications to return
     * @param offset Number of notifications to skip
     * @return NotificationListResponse containing total, unread counts and list of notifications
     */
    NotificationListResponse listUnreadNotifications(long userId, int limit, int offset);

    /**Create a new notification for a user.
     * @param req NotificationCreateRequest containing userId, message, and optional link
     * @return ID of the created notification
     */
    Long createNotification(NotificationCreateRequest req, Long UserId);

    /** Mark a specific notification as read.
     * @param notificationId ID of the notification to mark as read
     */
    void markOneRead(long notificationId);

    /** Mark all notifications as read for the current user.
     * This method assumes the current user context is available.
     */
    void markAllRead();
}
