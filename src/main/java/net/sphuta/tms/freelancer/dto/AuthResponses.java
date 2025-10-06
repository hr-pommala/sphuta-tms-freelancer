package net.sphuta.tms.freelancer.dto;

public class AuthResponses {
    public record JwtResponse(String token, String tokenType, String fullName, String email, Long id) {}
    public record ApiMessage(String message) {}
}
