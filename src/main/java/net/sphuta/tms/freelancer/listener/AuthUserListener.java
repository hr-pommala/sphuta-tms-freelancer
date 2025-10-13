package net.sphuta.tms.freelancer.listener;

import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.config.SpringContext;
import net.sphuta.tms.freelancer.entity.User;
import net.sphuta.tms.freelancer.entity.UserEntity;
import net.sphuta.tms.freelancer.repository.TmsUserRepository;

/**
 * AuthUserListener
 *
 * <p>This JPA listener ensures that whenever a new record is inserted
 * into the {@code auth_users} table (represented by {@link User}),
 * a corresponding record is automatically created in the {@code users} table
 * (represented by {@link UserEntity}).
 *
 * <p>The listener uses {@link SpringContext} to safely fetch
 * {@link TmsUserRepository} at runtime, avoiding circular bean dependencies.
 *
 * <p>Lifecycle Event: {@code @PrePersist} — triggered before an {@code auth_user}
 * record is persisted to the database.
 *
 * <p>Example:
 * <pre>
 *  New signup → auth_users → this listener → users (auto-sync)
 * </pre>
 *
 * @author ChatGPT
 * @since 2025-10-13
 */
@Slf4j
public class AuthUserListener {

    /**
     * PrePersist hook — executes just before {@code auth_user} is saved.
     * <p>This method maps the {@link User} entity into a {@link UserEntity}
     * and persists it to the {@code users} table.
     *
     * @param authUser the entity being persisted into {@code auth_users}
     */
    @PrePersist
    public void prePersist(User authUser) {

        // -------------------------------------------------------------
        // Fetch repository dynamically via SpringContext
        // (avoids circular dependency with JPA’s EntityManager)
        // -------------------------------------------------------------
        TmsUserRepository tmsUserRepo = SpringContext.getBean(TmsUserRepository.class);

        // -------------------------------------------------------------
        // Build a corresponding UserEntity for the users table
        // -------------------------------------------------------------
        UserEntity userEntity = UserEntity.builder()
                .email(authUser.getEmail()) // Copy email from auth_user
                .passwordHash(authUser.getPasswordHash()) // Copy password hash
                .fullName(authUser.getFirstName() +
                        (authUser.getLastName() != null ? " " + authUser.getLastName() : ""))
                .status("ACTIVE")           // Default status for new users
                .timezone("Asia/Kolkata")   // Default timezone (can adjust)
                .locale("en")               // Default locale
                .build();

        // -------------------------------------------------------------
        // Persist the mapped user entity to the users table
        // -------------------------------------------------------------
        try {
            tmsUserRepo.save(userEntity);
            log.info("✅ Synced auth_user [{}] into users table successfully", authUser.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to sync auth_user [{}] into users table: {}", authUser.getEmail(), e.getMessage(), e);
        }
    }
}
