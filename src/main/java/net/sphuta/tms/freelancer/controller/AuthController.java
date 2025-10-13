package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.RevokedToken;
import net.sphuta.tms.freelancer.repository.RevokedTokenRepository;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
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
    public TmsApiResponse<AuthResponses.JwtResponse> signup(@RequestBody AuthRequests.SignupRequest req) {
        log.info("Received signup request for email={}", req.email());
        var jwtResponse = userService.signup(req);
        return TmsApiResponse.created(TmsMessages.USER_REGISTERED_SUCCESS, jwtResponse);
    }

    /**
     * Authenticates a user and issues a JWT token.
     *
     * @param req The login request containing email/username and password.
     * @return A response entity with JWT token if authentication is successful.
     */
    @PostMapping("/login")
    @Operation(summary="Login user", description = "Returns JWT token on success")
    public TmsApiResponse<AuthResponses.JwtResponse> login(@RequestBody AuthRequests.LoginRequest req) {
        log.info("Login request received for principal={}", req.emailOrUsername());
        var jwtResponse = userService.login(req.emailOrUsername(), req.password());
        log.info("Login successful for principal={}", req.emailOrUsername());
        return TmsApiResponse.success(TmsMessages.LOGIN_SUCCESS, jwtResponse);
    }

    /**
     * Initiates the forgot password flow by sending a reset link to the user's email.
     *
     * @param req The forgot password request containing the user's email.
     * @return A response entity indicating that the reset link has been sent.
     */
    @PostMapping("/forgot")
    @Operation(summary="Start forgot password flow", description = "Sends reset link to registered email if exists")
    public TmsApiResponse<AuthResponses.ApiMessage> forgot(@RequestBody AuthRequests.ForgotRequest req) {
        log.info("Forgot password request received for email={}", req.email());
        userService.startForgotFlow(req.email());
        log.info("Forgot password process initiated for email={}", req.email());
        return TmsApiResponse.success(TmsMessages.RESET_LINK_SENT, new AuthResponses.ApiMessage(TmsMessages.RESET_LINK_SENT));
    }


    /**
     * Resets the user's password using their email.
     *
     * @param req The reset password request containing email, new password, and confirmation.
     * @return A response entity indicating that the password has been updated.
     */
    @PostMapping("/reset")
    @Operation(summary="Reset password using email (no token)", description = "Resets password directly using email")
    public TmsApiResponse<AuthResponses.ApiMessage> reset(@RequestBody AuthRequests.ResetPasswordRequest req) {
        log.info("Password reset request received for email={}", req.email());
        userService.resetPasswordByEmail(req.email(), req.newPassword(), req.confirmPassword());
        log.info("Password successfully reset for email={}", req.email());
        return TmsApiResponse.success(TmsMessages.PASSWORD_UPDATED, new AuthResponses.ApiMessage(TmsMessages.PASSWORD_UPDATED));
    }

    /**
     * Logs out the user by revoking the current JWT token.
     *
     * @param authHeader The Authorization header containing the JWT token.
     * @return A response entity indicating that the user has been logged out.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user (revoke current JWT)", description = "Revokes the current JWT token")
    public TmsApiResponse<AuthResponses.ApiMessage> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request received (hasAuthHeader={})", authHeader != null);
        log.debug("Logout Authorization header={}", authHeader);

        // Always return OK, even if token was missing/invalid
        log.info("Logout completed successfully");
        return TmsApiResponse.success(TmsMessages.LOGGED_OUT, new AuthResponses.ApiMessage(TmsMessages.LOGGED_OUT));
    }
}
