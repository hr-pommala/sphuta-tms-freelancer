package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
/**
     * Finds all notifications for a specific user, ordered by priority and creation date.
     *
     * @param userId   the ID of the user
     * @param pageable the pagination information
     * @return a list of notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findAllByUserIdOrdered(Long userId, Pageable pageable);
/**
     * Finds unread notifications for a specific user, ordered by priority and creation date.
     *
     * @param userId   the ID of the user
     * @param pageable the pagination information
     * @return a list of unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findUnreadByUserIdOrdered(Long userId, Pageable pageable);
/**
     * Counts the number of unread notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return the count of unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(Long userId);
/**
     * Counts the total number of notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return the total count of notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId")
    long countAllByUserId(Long userId);
}
