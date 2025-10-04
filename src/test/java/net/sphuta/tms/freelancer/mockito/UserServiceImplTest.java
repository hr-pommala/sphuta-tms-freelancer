package net.sphuta.tms.freelancer.mockito;

import net.sphuta.tms.freelancer.dto.AuthRequests;
import net.sphuta.tms.freelancer.dto.AuthResponses;
import net.sphuta.tms.freelancer.entity.User;
import net.sphuta.tms.freelancer.repository.PasswordResetTokenRepository;
import net.sphuta.tms.freelancer.repository.UserRepository;
import net.sphuta.tms.freelancer.security.JwtUtil;
import net.sphuta.tms.freelancer.service.EmailService;
import net.sphuta.tms.freelancer.service.impl.UserServiceImpl;
import net.sphuta.tms.freelancer.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock UserRepository userRepo;
    @Mock PasswordResetTokenRepository tokenRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock EmailService emailService;
    @InjectMocks UserServiceImpl userService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void signup_withEncryptedPassword_success() {
        String plainPassword = "test1234";
        String encryptedPassword = EncryptionUtil.decrypt(EncryptionUtil.decrypt(plainPassword)); // simulate double encryption for test
        AuthRequests.SignupRequest req = new AuthRequests.SignupRequest("John", "Doe", "john@example.com", encryptedPassword, encryptedPassword, null, null);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("token");
        AuthResponses.JwtResponse resp = userService.signup(req);
        assertEquals("token", resp.token());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void login_withEncryptedPassword_success() {
        String plainPassword = "test1234";
        String encryptedPassword = EncryptionUtil.decrypt(EncryptionUtil.decrypt(plainPassword)); // simulate double encryption for test
        User user = User.builder().email("john@example.com").passwordHash("hashed").roles(Set.of("ROLE_USER")).build();
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("token");
        AuthResponses.JwtResponse resp = userService.login("john@example.com", encryptedPassword);
        assertEquals("token", resp.token());
    }
}

