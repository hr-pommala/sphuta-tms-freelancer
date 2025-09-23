package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unified DTO for Project operations (create, update, view).
 *
 * <p>Uses validation groups + Swagger @Schema access modes to
 * control what appears in request vs response.</p>
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TmsProjectDto(

        /** Project identifier (response only). */
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
//        @Null(groups = {Create.class}, message = "id is auto-generated")
        int id,

        /** Linked client info (response only). */
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        TmsClientDto client,

        /** Foreign key: owning client ID (request only). */
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotNull(groups = {Create.class}, message = "clientId must not be null")
        Integer clientId,

        /**
         * NEW: Denormalized owner user id copied from the client (response only).
         */
        @Schema(description = "Owner user id (copied from client)", accessMode = Schema.AccessMode.READ_ONLY)
        Integer userId,


        /** Human-readable project name. Required on create. */
        @NotBlank(groups = {Create.class}, message = "projectName must not be blank")
        String projectName,

        /** Optional short code (unique per client). */
        @Size(max = 100)
        String code,

        /** Hourly billing rate. Must be > 0 if provided. */
        @NotNull(groups = {Create.class}, message = "hourlyRate must not be null")
        @DecimalMin(value = "0.01", inclusive = true, message = "hourlyRate must be > 0")
        BigDecimal hourlyRate,

        /** Optional start date (ISO format yyyy-MM-dd). */
        LocalDate startDate,

        /** Optional end date; must not be before start date. */
        LocalDate endDate,

        /** Free-form description, max 10k characters. */
        @Size(max = 10000)
        String description,

        /** Active status flag; defaults to true on create. */
        Boolean isActive,

        /** Timestamp when record was created (response only). */
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        String createdDt,

        /** Timestamp when record was last updated (response only). */
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        String updatedDt

) {
    /** Validation groups. */
    public interface Create {}
    public interface Update {}
}
