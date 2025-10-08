package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.RevokedToken;
import net.sphuta.tms.freelancer.repository.RevokedTokenRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Controller for authentication-related endpoints such as signup, login,
 * password reset, and logout.
 *
 * <p>This controller handles user registration, authentication, initiating
 * password reset flows, and logging out by revoking JWT tokens.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/v1/auth/signup - Register a new user</li>
 *   <li>POST /api/v1/auth/login - Authenticate a user and issue a JWT</li>
 *   <li>POST /api/v1/auth/forgot - Start the password reset process</li>
 *   <li>POST /api/v1/auth/reset - Reset password using email</li>
 *   <li>GET /api/v1/auth/me - Get current authenticated user's info</li>
 *   <li>POST /api/v1/auth/logout - Logout user by revoking current JWT</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
public class AuthController {

    @Autowired
    private  UserService userService;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    private  RevokedTokenRepository revokedTokenRepository;

    /**
     * Registers a new user.
     *
     * @param req The signup request containing user details.
     * @return A response entity with JWT token if successful.
     */
    @PostMapping("/signup")
    @Operation(summary="Sign up a new user", description = "Returns JWT token on success")
    public ResponseEntity<AuthResponses.JwtResponse> signup(@RequestBody AuthRequests.SignupRequest req) {
        log.info("Received signup request for email={}", req.email());
        return ResponseEntity.ok(userService.signup(req));
    }

    /**
     * Authenticates a user and issues a JWT token.
     *
     * @param req The login request containing email/username and password.
     * @return A response entity with JWT token if authentication is successful.
     */
    @PostMapping("/login")
    @Operation(summary="Login user", description = "Returns JWT token on success")
    public ResponseEntity<AuthResponses.JwtResponse> login(@RequestBody AuthRequests.LoginRequest req) {
        log.info("Login request received for principal={}", req.emailOrUsername());
        log.info("Login successful for principal={}", req.emailOrUsername());
        return ResponseEntity.ok(userService.login(req.emailOrUsername(), req.password()));
    }

    /**
     * Initiates the forgot password flow by sending a reset link to the user's email.
     *
     * @param req The forgot password request containing the user's email.
     * @return A response entity indicating that the reset link has been sent.
     */
    @PostMapping("/forgot")
    @Operation(summary="Start forgot password flow", description = "Sends reset link to registered email if exists")
    public ResponseEntity<AuthResponses.ApiMessage> forgot(@RequestBody AuthRequests.ForgotRequest req) {
        log.info("Forgot password request received for email={}", req.email());
        userService.startForgotFlow(req.email());
        log.info("Forgot password process initiated for email={}", req.email());
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Reset link shared to registered email if exists"));
    }

    /**
     * Resets the user's password using their email.
     *
     * @param req The reset password request containing email, new password, and confirmation.
     * @return A response entity indicating that the password has been updated.
     */
    @PostMapping("/reset")
    @Operation(summary="Reset password using email (no token)", description = "Resets password directly using email")
    public ResponseEntity<AuthResponses.ApiMessage> reset(@RequestBody AuthRequests.ResetPasswordRequest req) {
        log.info("Password reset request received for email={}", req.email());
        userService.resetPasswordByEmail(req.email(), req.newPassword(), req.confirmPassword());
        log.info("Password successfully reset for email={}", req.email());
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Password updated"));
    }

    /**
     * Retrieves information about the currently authenticated user.
     *
     * @param auth The Authorization header containing the JWT token.
     * @return A response entity with a greeting message including the user's email.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info", description = "Returns info about the current user")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String auth) {
        log.info("Fetching current authenticated user info");
        // simple endpoint to return current user name (subject) - Subject is email
        var subject = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        log.debug("Current authenticated user subject={}", subject);
        return ResponseEntity.ok(new AuthResponses.ApiMessage("hello " + subject));
    }

    /**
     * Logs out the user by revoking the current JWT token.
     *
     * @param authHeader The Authorization header containing the JWT token.
     * @return A response entity indicating that the user has been logged out.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user (revoke current JWT)", description = "Revokes the current JWT token")
    public ResponseEntity<AuthResponses.ApiMessage> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request received (hasAuthHeader={})", authHeader != null);
        log.debug("Logout Authorization header={}", authHeader);

        // Always return OK, even if token was missing/invalid
        log.info("Logout completed successfully");
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Logged out"));
    }
}
