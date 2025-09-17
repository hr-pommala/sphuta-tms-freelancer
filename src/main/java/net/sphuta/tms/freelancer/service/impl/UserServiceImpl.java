package net.sphuta.tms.freelancer.service.impl;


import lombok.RequiredArgsConstructor;
import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.PasswordResetToken;
import net.sphuta.tms.freelancer.entity.User;
import net.sphuta.tms.freelancer.repository.PasswordResetTokenRepository;
import net.sphuta.tms.freelancer.repository.UserRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.EmailService;
import net.sphuta.tms.freelancer.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService; // simple console email service

    @Override
    public AuthResponses.JwtResponse signup(AuthRequests.SignupRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        String username = (req.firstName() + "." + Optional.ofNullable(req.lastName()).orElse("")).toLowerCase().replaceAll("\\s+","");
        var user = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .username(username)
                .passwordHash(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .countryCode(req.countryCode())
                .roles(Set.of("ROLE_USER"))
                .createdAt(Instant.now())
                .build();
        user = userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        return new AuthResponses.JwtResponse(token,"Bearer", user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""), user.getEmail());
    }

    @Override
    public AuthResponses.JwtResponse login(String emailOrUsername, String password) {
        var opt = userRepo.findByEmail(emailOrUsername);
        if (opt.isEmpty()) opt = userRepo.findByUsername(emailOrUsername);
        var user = opt.orElseThrow(() -> new IllegalArgumentException("User doesn't exist"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        String token = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        return new AuthResponses.JwtResponse(token,"Bearer", user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""), user.getEmail());
    }

    @Override
    public void startForgotFlow(String email) {
        var user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email doesn't exist"));
        tokenRepo.deleteByEmail(email); // remove previous tokens
        String token = UUID.randomUUID().toString();
        var prt = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiry(Instant.now().plusSeconds(60 * 30)) // 30 minutes
                .used(false)
                .build();
        tokenRepo.save(prt);
        // send email (console for dev)
        String link = "http://localhost:5173/reset?token=" + token;
        emailService.send(email, "Password reset", "Reset link: " + link);
    }
    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email doesn't exist"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // optionally: invalidate any outstanding reset tokens
        tokenRepo.deleteByEmail(email);
    }


}
