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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    @Column(nullable = false, length = 320)
    private String email;

    @NotBlank(message = "Password hash is mandatory")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "Full name is mandatory")
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @NotBlank(message = "Status is mandatory")
    @Column(length = 20, nullable = false)
    private String status;

    /**
     * Default must be declared with @Builder.Default so Lombok's builder honours it.
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @NotBlank(message = "Timezone is mandatory")
    @Column(length = 64, nullable = false)
    private String timezone;

    @NotBlank(message = "Locale is mandatory")
    @Column(length = 20, nullable = false)
    private String locale;

    // NEW: ISO-4217 3-char currency
    @Column(length = 3)
    private String currency;

    // NEW: avatar URL
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    // NEW: soft active flag, default true
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
