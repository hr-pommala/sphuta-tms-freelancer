package net.sphuta.tms.freelancer.service.impl;

import net.sphuta.tms.freelancer.dto.NotificationCreateRequest;
import net.sphuta.tms.freelancer.dto.NotificationItem;
import net.sphuta.tms.freelancer.dto.NotificationListResponse;
import net.sphuta.tms.freelancer.dto.NotificationCount;
import net.sphuta.tms.freelancer.entity.Notification;
import net.sphuta.tms.freelancer.repository.NotificationRepository;
import net.sphuta.tms.freelancer.service.NotificationService;
import net.sphuta.tms.freelancer.util.NotificationMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;

    public NotificationServiceImpl(NotificationRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse listNotifications(long userId, int limit, int offset) {
        if (limit <= 0) limit = 20;
        if (offset < 0) offset = 0;
        int page = offset / Math.max(1, limit);
        var pageable = PageRequest.of(page, Math.max(1, limit));

        List<Notification> items = repo.findAllByUserIdOrdered(userId, pageable);

        long total = repo.countAllByUserId(userId);
        long unread = repo.countUnreadByUserId(userId);

        List<NotificationItem> dtoItems = items.stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());

        return new NotificationListResponse(new NotificationCount(total, unread), dtoItems);
    }

    @Override
    @Transactional
    public Long createNotification(NotificationCreateRequest req) {
        Notification entity = NotificationMapper.toEntity(req);
        Notification saved = repo.save(entity);
        return saved.getId();
    }

    @Override
    @Transactional
    public void markOneRead(long notificationId) {
        repo.findById(notificationId).ifPresent(n -> {
            if (!n.isRead()) {
                n.setRead(true);
                repo.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllRead() {
        List<Notification> unread = repo.findAll().stream()
                .filter(n -> !n.isRead())
                .toList();
        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setRead(true));
            repo.saveAll(unread);
        }
    }
}
