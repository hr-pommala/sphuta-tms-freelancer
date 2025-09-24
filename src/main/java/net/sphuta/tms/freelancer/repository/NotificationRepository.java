package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findAllByUserIdOrdered(Long userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findUnreadByUserIdOrdered(Long userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId")
    long countAllByUserId(Long userId);
}
