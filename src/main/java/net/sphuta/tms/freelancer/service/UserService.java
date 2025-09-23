package net.sphuta.tms.freelancer.service;


import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;

public interface UserService {

    AuthResponses.JwtResponse signup(AuthRequests.SignupRequest req);

    AuthResponses.JwtResponse login(String emailOrUsername, String password);

    void startForgotFlow(String email);

    // NEW: reset by email (no token)
    void resetPasswordByEmail(String email, String newPassword);

}
