package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.RevokedToken;
import net.sphuta.tms.freelancer.repository.RevokedTokenRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.UserService;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RevokedTokenRepository revokedTokenRepository;

    @PostMapping("/signup")
    @Operation(summary="Sign up a new user")
    public ResponseEntity<AuthResponses.JwtResponse> signup(@RequestBody AuthRequests.SignupRequest req) {
        return ResponseEntity.ok(userService.signup(req));
    }

    @PostMapping("/login")
    @Operation(summary="Login user")
    public ResponseEntity<AuthResponses.JwtResponse> login(@RequestBody AuthRequests.LoginRequest req) {
        return ResponseEntity.ok(userService.login(req.emailOrUsername(), req.password()));
    }

    @PostMapping("/forgot")
    @Operation(summary="Start forgot password flow")
    public ResponseEntity<AuthResponses.ApiMessage> forgot(@RequestBody AuthRequests.ForgotRequest req) {
        userService.startForgotFlow(req.email());
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Reset link shared to registered email if exists"));
    }

    @PostMapping("/reset")
    @Operation(summary="Reset password using email (no token)")
    public ResponseEntity<AuthResponses.ApiMessage> reset(@RequestBody AuthRequests.ResetPasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords mismatch");
        }
        userService.resetPasswordByEmail(req.email(), req.newPassword());
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Password updated"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String auth) {
        // simple endpoint to return current user name (subject) - Subject is email
        var subject = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(new AuthResponses.ApiMessage("hello " + subject));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user (revoke current JWT)")
    public ResponseEntity<AuthResponses.ApiMessage> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Instant expiry = jwtUtil.getExpiryInstant(token);
                RevokedToken rt = RevokedToken.builder()
                        .token(token)
                        .expiry(expiry)
                        .revokedAt(Instant.now())
                        .build();
                revokedTokenRepository.save(rt);
            } catch (Exception e) {
                // token parsing failed, ignore (already invalid/expired)
            }
        }
        // Always return OK, even if token was missing/invalid
        return ResponseEntity.ok(new AuthResponses.ApiMessage("Logged out"));
    }
}
