// package net.sphuta.tms.freelancer.entity;
package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    // Auto-generated primary key.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The revoked JWT token string.
    @Column(nullable = false, unique = true, length = 2048)
    private String token;

    // when the token becomes invalid (expiry from JWT)
    @Column(nullable = false)
    private Instant expiry;

    // when the token was revoked (added to blacklist)
    @Column(nullable = false)
    private Instant revokedAt;
}
 