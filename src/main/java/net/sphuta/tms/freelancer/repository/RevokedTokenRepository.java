// package net.sphuta.tms.freelancer.repository;
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    void deleteByExpiryBefore(java.time.Instant cutoff);
}
