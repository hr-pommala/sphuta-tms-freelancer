package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationItem;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.dto.NotificationCount;
import net.sphuta.tms.freelancer.entity.Notification;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.NotificationRepository;
import net.sphuta.tms.freelancer.repository.UserRepository;
import net.sphuta.tms.freelancer.service.NotificationService;
import net.sphuta.tms.freelancer.util.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService.
 * Handles business logic for managing user notifications.
 * Methods are transactional to ensure data integrity.
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository repo;

    @Autowired
    private UserRepository userRepo; // To validate user existence

    /**
     * List unread notifications for a user with pagination.
     * Validates user existence, applies limit and offset.
     * Returns total and unread counts along with notification items.
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse listUnreadNotifications(long userId, int limit, int offset) {
        log.info("listUnreadNotifications called for userId={}, limit={}, offset={}", userId, limit, offset);
        // ✅ Validate user exists
        if (!userRepo.existsById(userId)) {
            log.warn("User not found: userId={}", userId);
            throw new NotFoundException("User with id " + userId + " doesn't exist");
        }

        if (limit <= 0) limit = 20;
        if (offset < 0) offset = 0;
        int page = offset / Math.max(1, limit);
        var pageable = PageRequest.of(page, Math.max(1, limit));

        List<Notification> items = repo.findUnreadByUserIdOrdered(userId, pageable);

        long total = repo.countAllByUserId(userId);
        long unread = repo.countUnreadByUserId(userId);

        List<NotificationItem> dtoItems = items.stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());

        log.debug("listUnreadNotifications returning total={}, unread={}, returnedItems={}", total, unread, dtoItems.size());
        return new NotificationListResponse(new NotificationCount(total, unread), dtoItems);
    }

    /**
     * Create a new notification for a user.
     * Validates user existence before creation.
     * Returns the ID of the created notification.
     */
    @Override
    @Transactional
    public Long createNotification(NotificationCreateRequest req) {
        log.info("createNotification called for userId={}", req.userId());

        // ✅ Validate user exists
        if (!userRepo.existsById(req.userId())) {
            log.warn("Cannot create notification - user not found userId={}", req.userId());
            throw new NotFoundException("User with id " + req.userId() + " doesn't exist");
        }

        Notification entity = NotificationMapper.toEntity(req);
        Notification saved = repo.save(entity);
        log.debug("Notification created id={} for userId={}", saved.getId(), req.userId());
        return saved.getId();
    }

    /**
     * Mark a single notification as read by its ID.
     * If the notification is already read, no action is taken.
     */
    @Override
    @Transactional
    public void markOneRead(long notificationId) {
        log.info("markOneRead called for notificationId={}", notificationId);
        repo.findById(notificationId).ifPresent(n -> {
            if (!n.isRead()) {
                n.setRead(true);
                repo.save(n);
                log.debug("Notification {} marked as read", notificationId);
            }
        });
    }

    /**
     * Mark all notifications as read for all users.
     * Fetches all unread notifications and updates their status.
     */
    @Override
    @Transactional
    public void markAllRead() {
        log.info("markAllRead called - marking all unread notifications as read");
        List<Notification> unread = repo.findAll().stream()
                .filter(n -> !n.isRead())
                .toList();
        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setRead(true));
            repo.saveAll(unread);
            log.debug("Marked {} notifications as read", unread.size());
        }
    }


}
