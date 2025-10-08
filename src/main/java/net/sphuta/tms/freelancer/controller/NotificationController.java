package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notifications API controller.
 * Endpoints:
 *  - GET  /api/v1/users/{userId}/notifications         (list all)
 *  - POST /api/v1/users/{userId}/notifications         (create)
 *  - POST /api/v1/notifications/{notificationId}/read  (mark one as read)
 *  - POST /api/v1/notifications/read-all               (mark all read)
 */
@RestController
@Slf4j
@RequestMapping("/api/v1")
public class NotificationController {

    @Autowired
    private NotificationService svc;

    /** List unread notifications for user (with counts).
     * Supports pagination via limit and offset query parameters.
     * Default limit is 20, default offset is 0.
     * Returns total count and list of notifications.
     * Example: /api/v1/users/123/notifications?limit=10&offset=0
     */
    @GetMapping("/users/{userId}/notifications")
    @Operation(summary = "List unread notifications for a user with pagination",
            description = "Fetches unread notifications for the specified user. Supports pagination via limit and offset query parameters.")
    public ResponseEntity<NotificationListResponse> listNotifications(
            @PathVariable("userId") long userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset
    ) {
        log.info("Fetching notifications for userId={} with limit={} and offset={}", userId, limit, offset);
        NotificationListResponse resp = svc.listUnreadNotifications(userId, limit, offset);
        log.debug("Fetched {} notifications for userId={}", resp.items().size(), userId);
        return ResponseEntity.ok(resp);
    }

    /** Create a new notification for user.
     * Expects JSON body with userId, message, and optional link.
     * Returns the ID of the created notification.
     */
    @PostMapping("/users/{userId}/notifications")
    @Operation(summary = "Create a new notification for a user",
            description = "Creates a new notification for the specified user. Expects a JSON body with userId, message, and optional link.")
    public ResponseEntity<Map<String, Object>> createNotification(
            @PathVariable("userId") long userId,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        log.info("Create notification request received for userId={}", userId);
        log.debug("Notification payload: {}", request);

        if (!Long.valueOf(userId).equals(request.userId())) {
            log.warn("UserId mismatch: path userId={} payload userId={}", userId, request.userId());
            return ResponseEntity.badRequest().body(Map.of("error", "userId mismatch between path and payload"));
        }
        Long id = svc.createNotification(request);
        log.info("Notification created successfully with id={} for userId={}", id, userId);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /** Mark a single notification as read by ID.
     * Returns confirmation message.
     * Example: /api/v1/notifications/456/read
     */
    @PostMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Mark a single notification as read",
            description = "Marks the specified notification as read by its ID.")
    public ResponseEntity<String> markOneRead(@PathVariable("notificationId") long notificationId) {
        log.info("Marking notificationId={} as read", notificationId);
        svc.markOneRead(notificationId);
        log.debug("Notification {} marked as read", notificationId);
        return ResponseEntity.ok("Notification " + notificationId + " marked as read");
    }

    /** Mark all notifications as read (all users).
     * Returns HTTP 200 with no body.
     * Example: /api/v1/notifications/read-all
     */
    @PostMapping("/notifications/read-all")
    @Operation(summary = "Mark all notifications as read",
            description = "Marks all unread notifications as read for all users.")
    public ResponseEntity<Void> markAllRead() {
        log.info("Marking all notifications as read");
        svc.markAllRead();
        log.debug("All unread notifications marked as read");
        return ResponseEntity.ok().build();
    }
}
