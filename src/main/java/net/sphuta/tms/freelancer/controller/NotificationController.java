package net.sphuta.tms.freelancer.controller;

import net.sphuta.tms.freelancer.dto.AckRequest;
import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.service.NotificationService;
import net.sphuta.tms.freelancer.service.NotificationService.AckResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notifications API controller.
 * Endpoints:
 *  - GET  /api/v1/users/{userId}/notifications
 *  - POST /api/v1/users/{userId}/notifications  (create)
 *  - POST /api/v1/users/{userId}/notifications/_/ack?t=...  (ack single)
 *  - POST /api/v1/users/{userId}/notifications:read-all   (mark all read)
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/notifications")
public class NotificationController {

    private final NotificationService svc;

    public NotificationController(NotificationService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<NotificationListResponse> listNotifications(
            @PathVariable("userId") long userId,
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset
    ) {
        NotificationListResponse resp = svc.listNotifications(userId, unreadOnly, limit, offset);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(
            @PathVariable("userId") long userId,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        // sanity: userId path param must match request.userId (prevent mismatch)
        if (!Long.valueOf(userId).equals(request.userId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId mismatch between path and payload"));
        }
        Long id = svc.createNotification(request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PostMapping("/_/ack")
    public ResponseEntity<Map<String, Object>> ackSingle(
            @PathVariable("userId") long userId,
            @RequestParam("t") String token
    ) {
        AckResult r = svc.ackNotification(userId, token);
        return ResponseEntity.ok(Map.of("updated", r.updated(), "unread", r.unread()));
    }

    @PostMapping(path = "/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(@PathVariable("userId") long userId) {
        AckResult r = svc.markAllRead(userId);
        return ResponseEntity.ok(Map.of("updated", r.updated(), "unread", r.unread()));
    }
}
