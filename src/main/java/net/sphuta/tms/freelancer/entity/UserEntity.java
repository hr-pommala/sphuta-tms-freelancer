package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a user entity mapped to 'users' table.
 * Includes fields for user details and audit timestamps.
 * Enforces unique email constraint.
 * Uses Lombok for boilerplate code reduction.
 * Implements JPA lifecycle hooks for automatic timestamp management.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity {

    // Primary key with auto-increment strategy
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Unique email with validation
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    @Column(nullable = false, length = 320)
    private String email;

    // Password hash with validation
    @NotBlank(message = "Password hash is mandatory")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Full name with validation
    @NotBlank(message = "Full name is mandatory")
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    // Optional phone number
    @Column(length = 50)
    private String phone;

    // Optional country code
    @NotBlank(message = "Status is mandatory")
    @Column(length = 20, nullable = false)
    private String status;

    /**
     * Default must be declared with @Builder.Default so Lombok's builder honours it.
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    //timezone with validation
    @NotBlank(message = "Timezone is mandatory")
    @Column(length = 64, nullable = false)
    private String timezone;

    //locale with validation
    @NotBlank(message = "Locale is mandatory")
    @Column(length = 20, nullable = false)
    private String locale;

    //ISO-4217 3-char currency
    @Column(length = 3)
    private String currency;

    //avatar URL
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    //soft active flag, default true
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Audit timestamps
    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    // Last update timestamp
    @Column(name = "updated_dt")
    private LocalDateTime updateDt;

    // Deletion timestamp for soft deletes
    @Column(name = "deleted_dt")
    private LocalDateTime deletedDt;

    //JPA lifecycle hooks for automatic timestamp management
    @PrePersist
    protected void onCreate() {
        this.createdDt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    //
    @PreUpdate
    protected void onUpdate() {
        this.updateDt = LocalDateTime.now();
    }
}
