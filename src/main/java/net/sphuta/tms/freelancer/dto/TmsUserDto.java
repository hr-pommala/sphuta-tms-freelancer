package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TmsUserDto", description = "User unified DTO for requests and responses")
public record TmsUserDto(

        @Schema(description = "User ID (auto-incremented)", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
        int id,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 320)
        @Schema(description = "Primary email", example = "user@example.com")
        String email,

        @NotBlank(message = "passwordHash is required")
        @Schema(description = "Password hash (write-only)", accessMode = Schema.AccessMode.WRITE_ONLY)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String passwordHash,

        @NotBlank(message = "fullName is required")
        @Size(max = 200)
        @Schema(description = "Full name", example = "Swathi Medisetti")
        String fullName,

        @Size(max = 50)
        @Schema(description = "Phone", example = "+1-555-0100")
        String phone,

        @NotBlank(message = "status is required")
        @Size(max = 20)
        @Schema(description = "User status", example = "ACTIVE")
        String status,

        @Schema(description = "Email verified flag", example = "false")
        Boolean emailVerified,

        @NotBlank(message = "timezone is required")
        @Size(max = 64)
        @Schema(description = "Timezone", example = "Asia/Kolkata")
        String timezone,

        @NotBlank(message = "locale is required")
        @Size(max = 20)
        @Schema(description = "Locale", example = "en")
        String locale,

        // NEW fields
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO-4217 (e.g., USD)")
        @Size(max = 3)
        @Schema(description = "Currency code", example = "USD")
        String currency,

        @Schema(description = "Avatar URL", example = "https://.../avatar.png")
        String avatarUrl,

        @Schema(description = "Is active flag", example = "true")
        Boolean isActive,

        // audit
        @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdDt,

        @Schema(description = "Record last updated timestamp", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedDt

) {}
