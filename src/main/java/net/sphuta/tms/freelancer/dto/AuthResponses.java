package net.sphuta.tms.freelancer.dto;

public class AuthResponses {
    public record JwtResponse(String token, String tokenType, String fullName, String email) {}
    public record ApiMessage(String message) {}
}
