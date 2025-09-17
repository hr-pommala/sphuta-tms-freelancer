package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String token;

    @Column(nullable=false)
    private String email; // the user email the token belongs to

    @Column(nullable=false)
    private Instant expiry;

    private boolean used;
}
