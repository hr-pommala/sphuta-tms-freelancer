package net.sphuta.tms.freelancer.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Data Transfer Objects (DTOs) for authentication-related requests.
 *
 * <p>This class contains static inner classes representing the structure
 * of various authentication requests such as signup, login, password reset,
 * and forgot password. Each inner class uses validation annotations to
 * enforce required fields and formats.
 *
 * <p>Inner Classes:
 * <ul>
 *   <li>SignupRequest - Request body for user registration</li>
 *   <li>LoginRequest - Request body for user login</li>
 *   <li>ForgotRequest - Request body to initiate password reset</li>
 *   <li>ResetPasswordRequest - Request body to reset password using email</li>
 * </ul>
 */

public class AuthRequests {
    public record SignupRequest(

            @NotBlank(message="First name required")
            String firstName,

            String lastName,

            @NotBlank
            @Email
            String email,

            @NotBlank
            String password,

            @NotBlank
            String confirmPassword,

            String phone,

            String countryCode
    ) {}

    public record LoginRequest(
            @NotBlank
            String emailOrUsername,

            @NotBlank
            String password
    ) {}

    public record ForgotRequest(
            @NotBlank
            @Email
            String email
    ) {}

    // UPDATED: use email instead of token
    public record ResetPasswordRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String newPassword,

            @NotBlank
            String confirmPassword
    ) {}
}
