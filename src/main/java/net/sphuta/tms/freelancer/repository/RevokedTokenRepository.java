// package net.sphuta.tms.freelancer.repository;
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link RevokedToken} entities.
 *
 * <p>This interface extends {@link JpaRepository}, providing CRUD operations
 * and custom query methods for revoked tokens.</p>
 */
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    /**
     * Finds a revoked token by its token string.
     *
     * @param token the token string to search for
     * @return an Optional containing the found RevokedToken, or empty if not found
     */
    Optional<RevokedToken> findByToken(String token);

    /**
     * Deletes all revoked tokens that have an expiry date before the specified cutoff.
     *
     * @param cutoff the cutoff Instant; tokens expiring before this will be deleted
     */
    void deleteByExpiryBefore(java.time.Instant cutoff);
}
