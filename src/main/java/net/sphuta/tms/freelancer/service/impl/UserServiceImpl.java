package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.PasswordResetToken;
import net.sphuta.tms.freelancer.entity.RevokedToken;
import net.sphuta.tms.freelancer.entity.User;
import net.sphuta.tms.freelancer.repository.PasswordResetTokenRepository;
import net.sphuta.tms.freelancer.repository.RevokedTokenRepository;
import net.sphuta.tms.freelancer.repository.UserRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.EmailService;
import net.sphuta.tms.freelancer.service.UserService;
import net.sphuta.tms.freelancer.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Implementation of {@link UserService} containing user-related business logic.
 * <p>This class handles signup, login, forgot/reset password flows and logout (token revocation).
 * Logging has been added for observability
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    /** Repository for managing user persistence.
     * Note: Do not make this static, as that would prevent Spring from injecting the proxy instance.
     */
    @Autowired
    private  UserRepository userRepo;

    @Autowired
    private  PasswordResetTokenRepository tokenRepo;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    private  EmailValidationService emailValidationService;
    @Autowired
    private  EmailService emailService;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    /**
     * Create a new user account and return a JWT response.
     * @param req signup request from client (contains encrypted passwords)
     * @return JwtResponse containing token and user metadata
     * @throws IllegalArgumentException if passwords mismatch or email already exists
     */
    @Override
    public AuthResponses.JwtResponse signup(AuthRequests.SignupRequest req) {
        log.info("Signup request received for email={}", req.email());
        // Decrypt passwords from UI
        String decryptedPassword = EncryptionUtil.decrypt(req.password());
        String decryptedConfirmPassword = EncryptionUtil.decrypt(req.confirmPassword());

        if (!decryptedPassword.equals(decryptedConfirmPassword)) {
            log.warn("Signup failed for email={} - passwords do not match", req.email());
            throw new IllegalArgumentException(TmsMessages.PASSWORD_MISMATCH);
        }
        if (userRepo.existsByEmail(req.email())) {
            log.warn("Signup failed for email={} - email already registered", req.email());
            throw new IllegalArgumentException(TmsMessages.EMAIL_ALREADY_EXISTS);
        }
        // ✅ Validate email across both tables
        emailValidationService.validateEmailUnique(req.email());

        String username = (req.firstName() + "." + Optional.ofNullable(req.lastName()).orElse("")).toLowerCase().replaceAll("\\s+","");
        var user = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .username(username)
                .passwordHash(passwordEncoder.encode(decryptedPassword))
                .phone(req.phone())
                .countryCode(req.countryCode())
                .roles(Set.of("ROLE_USER"))
                .createdDt(Instant.now())
                .build();
        user = userRepo.save(user);
        log.debug("User persisted id={} email={}", user.getId(), user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        log.info("Signup successful for email={} id={}", user.getEmail(), user.getId());
        return new AuthResponses.JwtResponse(token,"Bearer", user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""), user.getEmail(), user.getId());
    }

    /**
     * Authenticate user (by email or username) and return JWT response.
     *
     * @param emailOrUsername the email or username provided
     * @param password        encrypted password from client
     * @return JwtResponse containing token and user metadata
     * @throws IllegalArgumentException if user not found or credentials invalid
     */
    @Override
    public AuthResponses.JwtResponse login(String emailOrUsername, String password) {
        log.info("Login attempt for principal={}", emailOrUsername);
        // Decrypt password from UI
        log.debug("Decrypting password for principal={}", emailOrUsername);
        String decryptedPassword = EncryptionUtil.decrypt(password);
        log.debug("Password decryption completed for principal={}", emailOrUsername);

        var opt = userRepo.findByEmail(emailOrUsername);
        if (opt.isEmpty()) {
            log.debug("No user found by email, trying username for principal={}", emailOrUsername);
            opt = userRepo.findByUsername(emailOrUsername);
        }

        var user = opt.orElseThrow(() -> {
            log.warn("Login failed - user not found for principal={}", emailOrUsername);
            return new IllegalArgumentException(TmsMessages.EMAIL_NOT_FOUND);
        });

        if (!passwordEncoder.matches(decryptedPassword, user.getPasswordHash())) {
            log.warn("Login failed - invalid credentials for user id={}", user.getId());
            throw new IllegalArgumentException(TmsMessages.INVALID_CREDENTIALS);
        }
        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        log.info("Login successful for user id={} email={}", user.getId(), user.getEmail());
        log.debug("Generated JWT token for user id={}", user.getId());

        return new AuthResponses.JwtResponse(token,"Bearer", user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""), user.getEmail(),user.getId());
    }

    /**
     * Start forgot-password flow: generate and persist a password reset token and send reset link.
     *
     * @param email registered user email
     * @throws IllegalArgumentException if email not registered
     */
    @Override
    public void startForgotFlow(String email) {
        log.info("Start forgot flow for email={}", email);

        var user = userRepo.findByEmail(email).orElseThrow(() -> {
            log.warn("Forgot flow requested for non-existent email={}", email);
            return new IllegalArgumentException(TmsMessages.EMAIL_NOT_FOUND);
        });

        tokenRepo.deleteByEmail(email); // remove previous tokens
        String token = UUID.randomUUID().toString();
        var prt = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiry(Instant.now().plusSeconds(60 * 30)) // 30 minutes
                .used(false)
                .build();
        tokenRepo.save(prt);
        // send email (console for dev)
        String link = "http://localhost:5173/reset?token=" + token;
        emailService.send(email, "Password reset", "Reset link: " + link);
        log.debug("Password reset token saved for email={} token={}", email, token);
        log.info("Password reset link sent to email={}", email);
    }

    /**
     * Reset a user's password using email (no token). Decrypts and stores new password hash.
     *
     * @param email           registered email
     * @param newPassword     encrypted new password
     * @param confirmPassword encrypted confirm password (must match newPassword)
     * @throws IllegalArgumentException if passwords mismatch or email not found
     */
    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword, String confirmPassword) {
        log.info("Reset password by email request for email={}", email);

        if (!Objects.equals(newPassword, confirmPassword)) {
            log.warn("Reset password failed for email={} - passwords mismatch", email);
            throw new IllegalArgumentException(TmsMessages.PASSWORD_MISMATCH);
        }

        // Decrypt password from UI
        String decryptedPassword = EncryptionUtil.decrypt(newPassword);
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Reset password failed - email not found: {}", email);
                    return new IllegalArgumentException(TmsMessages.EMAIL_NOT_FOUND);
                });
        user.setPasswordHash(passwordEncoder.encode(decryptedPassword));
        userRepo.save(user);
        log.debug("Password updated for user id={} email={}", user.getId(), email);

        // optionally: invalidate any outstanding reset tokens
        tokenRepo.deleteByEmail(email);
        log.info("Reset tokens (if any) cleared for email={}", email);
    }

    /**
     * Revoke the provided JWT by storing it in the revoked-tokens table.
     *
     * @param authHeader Authorization header (may be null)
     * @return ApiMessage indicating logout status
     */
    @Override
    public AuthResponses.ApiMessage logout(String authHeader) {
        log.info("Logout requested (auth header present={})", authHeader != null);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Instant expiry = jwtUtil.getExpiryInstant(token);
                RevokedToken rt = RevokedToken.builder()
                        .token(token)
                        .expiry(expiry)
                        .revokedAt(Instant.now())
                        .build();
                // <- call save on the bean instance, not the type
                revokedTokenRepository.save(rt);
                log.debug("Revoked token saved expiry={} tokenHash={}", expiry, (token.length() > 10 ? token.substring(0, 10) + "..." : token));
            } catch (Exception e) {
                // token parsing failed, ignore (already invalid/expired)
                log.warn("Failed to parse JWT during logout: {}", e.getMessage());
            }
        } else {
            log.debug("Logout called without Bearer token");
        }

        // Always return OK message, same text as controller previously returned
        log.info("Logout completed");
        return new AuthResponses.ApiMessage("Logged out");
    }


}
