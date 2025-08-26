package net.sphuta.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Container class grouping together the different DTO variants
 * (Create, Update, and View) used for Project entity operations.
 *
 * <p>Reason for grouping: keeps all request/response structures
 * related to Projects in a single file for discoverability and
 * reduces clutter in the DTO package.</p>
 */
public class ProjectDtos {

    /**
     * DTO for creating a new Project.
     *
     * <p>Constraints:</p>
     * <ul>
     *   <li>{@code clientId} is required (foreign key reference).</li>
     *   <li>{@code projectName} is required and unique per client.</li>
     *   <li>{@code code} is optional but must be unique per client if provided.</li>
     *   <li>{@code hourlyRate} must be > 0.</li>
     *   <li>{@code endDate} must be after or equal to {@code startDate} (validated at service/db level).</li>
     * </ul>
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Create {

        /** Client identifier (UUID in string form). Required. */
        @NotNull(message = "clientId must not be null")
        private String clientId;

        /** Human-readable project name. Required, unique per client. */
        @NotBlank(message = "projectName must not be blank")
        private String projectName;

        /** Optional short code; unique per client if provided. */
        @Size(max = 100)
        private String code;

        /** Hourly billing rate. Must be > 0. */
        @NotNull @DecimalMin(value="0.01", inclusive = true, message="hourlyRate must be > 0")
        private BigDecimal hourlyRate;

        /** Optional start date in ISO format (yyyy-MM-dd). */
        private LocalDate startDate;

        /** Optional end date; must not be before start date. */
        private LocalDate endDate;

        /** Free-form description, max 10k characters. */
        @Size(max = 10000)
        private String description;

        /** Status flag; defaults to active. */
        private Boolean isActive = Boolean.TRUE;
    }

    /**
     * DTO for updating a Project (full or partial).
     *
     * <p>Used for both PUT (full replace) and PATCH (partial update).</p>
     * <ul>
     *   <li>For PATCH: fields are optional; only provided ones are updated.</li>
     *   <li>For PUT: the controller enforces required fields.</li>
     * </ul>
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update {

        /** Client identifier. Required on PUT, optional on PATCH. */
        private String clientId;

        /** Project name. Optional on PATCH, required on PUT. */
        private String projectName;

        /** Optional short code; max length 100. */
        @Size(max=100)
        private String code;

        /** Hourly billing rate. Must be > 0 if present. */
        @DecimalMin(value="0.01", inclusive = true, message="hourlyRate must be > 0")
        private BigDecimal hourlyRate;

        /** Optional start date. */
        private LocalDate startDate;

        /** Optional end date. */
        private LocalDate endDate;

        /** Optional description, up to 10k characters. */
        @Size(max = 10000)
        private String description;

        /** Active status flag; null means no change (PATCH). */
        private Boolean isActive;
    }

    /**
     * DTO for returning a Project in API responses.
     *
     * <p>Includes:</p>
     * <ul>
     *   <li>Identifiers</li>
     *   <li>Client summary (nested {@link ClientDto})</li>
     *   <li>All primary project fields</li>
     *   <li>Timestamps for auditing</li>
     * </ul>
     *
     * <p>Annotated with {@link JsonInclude} to omit nulls from JSON output.</p>
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class View {

        /** Project identifier (UUID as string). */
        private String id;

        /** Linked client info (id + name only). */
        private ClientDto client;

        /** Project display name. */
        private String projectName;

        /** Optional short code. */
        private String code;

        /** Hourly billing rate. */
        private BigDecimal hourlyRate;

        /** Start date, if set. */
        private LocalDate startDate;

        /** End date, if set. */
        private LocalDate endDate;

        /** Free-form description. */
        private String description;

        /** Active flag. */
        private Boolean isActive;

        /** Timestamp when record was created (ISO-8601 string). */
        private String createdAt;

        /** Timestamp when record was last updated (ISO-8601 string). */
        private String updatedAt;
    }
}
