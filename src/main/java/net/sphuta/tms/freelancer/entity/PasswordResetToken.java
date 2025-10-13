package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    /** Auto-generated primary key. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique token string for password reset. */
    @Column(nullable=false, unique=true)
    private String token;

    /** The user email the token belongs to. */
    @Column(nullable=false)
    private String email; // the user email the token belongs to

    /** Expiry timestamp for the token. */
    @Column(nullable=false)
    private Instant expiry;

    /** Whether the token has been used. */
    private boolean used;
}
