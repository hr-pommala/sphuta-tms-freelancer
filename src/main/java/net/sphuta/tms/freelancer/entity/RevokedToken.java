// package net.sphuta.tms.freelancer.entity;
package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "revoked_tokens", indexes = {
        @Index(name = "idx_revoked_token_token", columnList = "token", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevokedToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String token;

    // when the token becomes invalid (expiry from JWT)
    @Column(nullable = false)
    private Instant expiry;

    @Column(nullable = false)
    private Instant revokedAt;
}
