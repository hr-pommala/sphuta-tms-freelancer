package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.repository.TmsUserRepository;
import net.sphuta.tms.freelancer.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * EmailValidationService
 *
 * <p>This service ensures that a given email address is unique
 * across both authentication users (auth_users) and profile users (users) tables.
 *
 * <p>It checks the existence of the email in:
 * <ul>
 *   <li>{@link UserRepository} → auth_users table</li>
 *   <li>{@link TmsUserRepository} → users table</li>
 * </ul>
 *
 * <p>If the email is found in either repository, an {@link IllegalArgumentException}
 * is thrown to prevent duplicate registrations.
 *
 * <p>✅ Used during user signup and account creation processes.
 *
 * @author ChatGPT
 * @since 2025-10-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailValidationService {

    /** Repository for auth_users table */
    private final UserRepository userRepo;

    /** Repository for users table */
    private final TmsUserRepository tmsUserRepo;

    /**
     * Validates that the given email does not exist in either the auth_users or users tables.
     *
     * @param email the email address to validate
     * @throws IllegalArgumentException if the email already exists in the system
     */
    public void validateEmailUnique(String email) {

        // -------------------------------------------------------------
        // 1️⃣ Check for existing email in auth_users (UserRepository)
        // -------------------------------------------------------------
        boolean existsInAuthUser = userRepo.existsByEmail(email);
        if (existsInAuthUser) {
            log.warn("⚠️ Email [{}] already exists in auth_users table.", email);
        }

        // -------------------------------------------------------------
        // 2️⃣ Check for existing email in users (TmsUserRepository)
        // -------------------------------------------------------------
        boolean existsInProfileUser = tmsUserRepo.findByEmail(email).isPresent();
        if (existsInProfileUser) {
            log.warn("⚠️ Email [{}] already exists in users table.", email);
        }

        // -------------------------------------------------------------
        // 3️⃣ If found in either table, throw a clear validation exception
        // -------------------------------------------------------------
        if (existsInAuthUser || existsInProfileUser) {
            log.error("❌ Email validation failed — [{}] already exists in the system.", email);
            throw new IllegalArgumentException("Email already exists in the system");
        }

        // -------------------------------------------------------------
        // 4️⃣ Otherwise, validation passed successfully
        // -------------------------------------------------------------
        log.info("✅ Email [{}] is unique and available for registration.", email);
    }
}
