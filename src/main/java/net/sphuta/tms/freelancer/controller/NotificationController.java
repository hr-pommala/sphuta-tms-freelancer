package net.sphuta.tms.freelancer.controller;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.service.NotificationService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService svc;

    public NotificationController(NotificationService svc) {
        this.svc = svc;
    }

    /** List unread notifications for user (with counts). */
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<NotificationListResponse> listNotifications(
            @PathVariable("userId") long userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset
    ) {
        NotificationListResponse resp = svc.listUnreadNotifications(userId, limit, offset);
        return ResponseEntity.ok(resp);
    }

    /** Create a new notification for user. */
    @PostMapping("/users/{userId}/notifications")
    public ResponseEntity<Map<String, Object>> createNotification(
            @PathVariable("userId") long userId,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        if (!Long.valueOf(userId).equals(request.userId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId mismatch between path and payload"));
        }
        Long id = svc.createNotification(request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /** Mark a single notification as read by ID. */
    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<String> markOneRead(@PathVariable("notificationId") long notificationId) {
        svc.markOneRead(notificationId);
        return ResponseEntity.ok("Notification " + notificationId + " marked as read");
    }

    /** Mark all notifications as read (all users). */
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllRead() {
        svc.markAllRead();
        return ResponseEntity.ok().build();
    }
}
