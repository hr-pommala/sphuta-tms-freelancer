package net.sphuta.tms.freelancer.dto;

/**
 * Data Transfer Objects (DTOs) for authentication-related responses.
 *
 * <p>This class contains static inner classes representing the structure
 * of various authentication responses such as JWT tokens and API messages.
 *
 * <p>Inner Classes:
 * <ul>
 *   <li>JwtResponse - Response body containing JWT token and user details</li>
 *   <li>ApiMessage - Generic response body for API messages</li>
 * </ul>
 */
public class AuthResponses {

    public record JwtResponse(

            String token,

            String tokenType,

            String fullName,

            String email,

            Long id) {}

    public record ApiMessage(
            String message
    ) {}
}
