package net.sphuta.tms.freelancer.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthRequests {
    public record SignupRequest(
            @NotBlank(message="First name required") String firstName,
            String lastName,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String confirmPassword,
            String phone,
            String countryCode
    ) {}

    public record LoginRequest(
            @NotBlank String emailOrUsername,
            @NotBlank String password
    ) {}

    public record ForgotRequest(@NotBlank @Email String email) {}

    // UPDATED: use email instead of token
    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String newPassword,
            @NotBlank String confirmPassword
    ) {}
}
