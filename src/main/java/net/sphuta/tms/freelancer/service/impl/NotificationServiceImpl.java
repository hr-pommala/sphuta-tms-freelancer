package net.sphuta.tms.freelancer.service.impl;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationItem;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.dto.NotificationCount;
import net.sphuta.tms.freelancer.entity.Notification;

import net.sphuta.tms.freelancer.repository.NotificationRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.NotificationService;

import io.jsonwebtoken.JwtException;
import net.sphuta.tms.freelancer.util.NotificationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the NotificationService interface.
 * Provides methods to list, create, and acknowledge notifications.
 */
@Service
public class NotificationServiceImpl implements NotificationService {


    private final NotificationRepository repo;
    private final JwtUtil jwtUtil;
    private final String ackBaseUrlFormat;
/*
     * Constructs a NotificationServiceImpl with the specified repository, JWT utility, and acknowledgment URL format.
     *
     * @param repo               the NotificationRepository for database operations
     * @param jwtUtil            the JwtUtil for token generation and parsing
     * @param ackBaseUrlFormat   the base URL format for acknowledgment links
     */
    public NotificationServiceImpl(NotificationRepository repo,
                                   JwtUtil jwtUtil,
                                   @Value("${app.notifications.ack-base-url:/api/v1/users/%d/notifications/_/ack?t=%s}") String ackBaseUrlFormat) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.ackBaseUrlFormat = ackBaseUrlFormat;
    }

    /**
     * Lists notifications for a user with optional filtering for unread only, and supports pagination.
     *
     * @param userId     the ID of the user
     * @param unreadOnly if true, only unread notifications are returned
     * @param limit      the maximum number of notifications to return
     * @param offset     the offset from which to start returning notifications
     * @return a NotificationListResponse containing the notifications and their counts
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse listNotifications(long userId, boolean unreadOnly, int limit, int offset) {
        if (limit <= 0) limit = 20;
        if (offset < 0) offset = 0;
        int page = offset / Math.max(1, limit);
        var pageable = PageRequest.of(page, Math.max(1, limit));

        List<Notification> items = unreadOnly
                ? repo.findUnreadByUserIdOrdered(userId, pageable)
                : repo.findAllByUserIdOrdered(userId, pageable);

        long total = repo.countAllByUserId(userId);
        long unread = repo.countUnreadByUserId(userId);

        List<NotificationItem> dtoItems = items.stream().map(n -> {
            String token = jwtUtil.createAckToken(n.getId());
            String ackUrl = String.format(ackBaseUrlFormat, n.getUserId(), token);
            return NotificationMapper.toDto(n, ackUrl);
        }).collect(Collectors.toList());

        return new NotificationListResponse(new NotificationCount(total, unread), dtoItems);
    }

/**     * Creates a new notification from the provided request.
     *
     * @param req the NotificationCreateRequest containing the notification details
     * @return the ID of the created notification
     */
    @Override
    @Transactional
    public Long createNotification(NotificationCreateRequest req) {
        Notification entity = NotificationMapper.toEntity(req);
        Notification saved = repo.save(entity);
        return saved.getId();
    }

/**     * Acknowledges (marks as read) a single notification using the provided token.
     *
     * @param userId the ID of the user
     * @param token  the acknowledgment token
     * @return an AckResult containing the number of updated notifications and the count of unread notifications after the update
     */
    @Override
    @Transactional
    public AckResult ackNotification(long userId, String token) {
        try {
            // Use explicit ack-token parsing method
            Long nid = jwtUtil.getNotificationIdFromAckToken(token);

            var opt = repo.findById(nid);
            if (opt.isEmpty()) return new AckResult(0, repo.countUnreadByUserId(userId));
            Notification n = opt.get();
            if (!n.getUserId().equals(userId)) {
                // token valid but not this user: do nothing
                return new AckResult(0, repo.countUnreadByUserId(userId));
            }
            if (!n.isRead()) {
                n.setRead(true);
                repo.save(n);
                return new AckResult(1, repo.countUnreadByUserId(userId));
            } else {
                return new AckResult(0, repo.countUnreadByUserId(userId));
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // invalid token
            return new AckResult(0, repo.countUnreadByUserId(userId));
        }
    }

/**     * Marks all notifications as read for the specified user.
     *
     * @param userId the ID of the user
     * @return an AckResult containing the number of updated notifications and the count of unread notifications after the update
     */
    @Override
    @Transactional
    public AckResult markAllRead(long userId) {
        List<Notification> unread = repo.findUnreadByUserIdOrdered(userId, PageRequest.of(0, Integer.MAX_VALUE));
        int updated = 0;
        for (Notification n : unread) {
            if (!n.isRead()) {
                n.setRead(true);
                updated++;
            }
        }
        if (updated > 0) repo.saveAll(unread);
        long unreadAfter = repo.countUnreadByUserId(userId);
        return new AckResult(updated, unreadAfter);
    }
}
