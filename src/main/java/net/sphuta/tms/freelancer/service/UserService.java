package net.sphuta.tms.freelancer.service;


import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;

public interface UserService {

    AuthResponses.JwtResponse signup(AuthRequests.SignupRequest req);

    AuthResponses.JwtResponse login(String emailOrUsername, String password);

    void startForgotFlow(String email);

    // ✅ UPDATED: now includes confirmPassword so logic can move from controller to service
    void resetPasswordByEmail(String email, String newPassword, String confirmPassword);

    // ✅ NEW: logout logic moved from controller to service
    AuthResponses.ApiMessage logout(String authHeader);

}
