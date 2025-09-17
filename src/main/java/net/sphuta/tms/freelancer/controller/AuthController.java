package net.sphuta.tms.freelancer.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.service.UserService;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final UserService userService;

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
        var subject = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(new AuthResponses.ApiMessage("hello " + subject));
    }
}
