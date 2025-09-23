package net.sphuta.tms.freelancer.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "profile_users")
@Access(AccessType.FIELD)         // IMPORTANT: use field access only
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    // Explicit column mapping and field access prevents duplicate mapping
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String phone;
    private String countryCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    private Instant createdAt;
}
