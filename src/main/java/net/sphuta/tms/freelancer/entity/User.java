package net.sphuta.tms.freelancer.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

/**
 * User entity representing a user profile in the system.
 * Mapped to the "profile_users" table in the database.
 * Uses field access for JPA to avoid duplicate mapping issues.
 */
@Entity
@Table(name = "profile_users")
@Access(AccessType.FIELD)         // IMPORTANT: use field access only
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Auto-generated primary key.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Explicit column mapping and field access prevents duplicate mapping
    @Column(name = "first_name", nullable = false)
    private String firstName;

    private String lastName;

    // Explicit column mapping and field access prevents duplicate mapping
    @Column(nullable = false, unique = true)
    private String email;

    // Explicit column mapping and field access prevents duplicate mapping
    @Column(unique = true)
    private String username;

    // Explicit column mapping and field access prevents duplicate mapping
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String phone;
    private String countryCode;

    // EAGER fetch to load roles immediately with user
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    private Instant createdAt;
}
