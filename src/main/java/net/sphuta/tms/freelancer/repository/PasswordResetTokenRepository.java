package net.sphuta.tms.freelancer.repository;


import net.sphuta.tms.freelancer.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for managing PasswordResetToken entities.
 * Extends JpaRepository to provide CRUD operations.
 * @see PasswordResetToken
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /** Find a PasswordResetToken by its token string.
     *
     * @param token the token string
     * @return an Optional containing the PasswordResetToken if found, or empty if not found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /** Delete a PasswordResetToken by the associated email.
     *
     * @param email the email associated with the token to be deleted
     */
    void deleteByEmail(String email);
}
