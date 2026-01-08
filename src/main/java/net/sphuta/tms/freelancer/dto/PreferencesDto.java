package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import net.sphuta.tms.freelancer.enums.Rounding;
import net.sphuta.tms.freelancer.enums.WeekStart;

import java.time.LocalDateTime;

/**
 * Unified DTO for user preferences.
 * <p>
 * - Request: clients send first 4 fields.
 * - Response: server returns all 5 fields (including read-only updatedAt).
 */
public record PreferencesDto(

        @NotNull(message = "UserId is required")
        @Min(value = 1, message = "UserId must be greater than 0")
        int userId,

        @Pattern(regexp = "YYYY-MM-DD", message = "Date format must be YYYY-MM-DD")
        String dateFormat,

        WeekStart weekStartsOn,

        Rounding rounding,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime updateDt

) {}
