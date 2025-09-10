package net.sphuta.tms.freelancer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.entity.ProjectEntity;

import java.time.format.DateTimeFormatter;

/**
 * Utility class for mapping between {@link ClientEntity}, {@link ProjectEntity}
 * and their corresponding DTOs {@link TmsClientDto}, {@link TmsProjectDto}.
 * <p>
 * This is a stateless, final utility with static methods only. It is
 * responsible for converting entities into DTOs for API responses and vice versa.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Convert {@link ClientEntity} → {@link TmsClientDto}</li>
 *   <li>Convert {@link ProjectEntity} → {@link TmsProjectDto}</li>
 *   <li>Build {@link ProjectEntity} from {@link TmsProjectDto} when creating projects</li>
 *   <li>Apply partial updates from {@link TmsProjectDto} into {@link ProjectEntity}</li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // Prevent instantiation (utility class)
public final class TmsProjectMapper {

    /** Formatter for date-time values (audit timestamps). ISO-8601 format. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /* ---------- Client Mapping ---------- */

    /**
     * Maps a {@link ClientEntity} to a {@link TmsClientDto}.
     * <p>
     * - Builds a slim DTO with only id and name fields.

     * - Prefers companyName as the display name. If absent, uses firstName + lastName.
     *
     * @param e Client entity to map
     * @return slim client DTO for dropdowns or lists
     */
    public static TmsClientDto toClientDto(ClientEntity e) {
        if (e == null) return null;
        log.debug("Mapping ClientEntity -> TmsClientDto (id={})", e.getId());

        // Build display name: prefer companyName, else fallback to first + last name.
        String displayName = (e.getCompanyName() != null && !e.getCompanyName().isBlank())
                ? e.getCompanyName()
                : ((e.getFirstName() != null ? e.getFirstName() : "")
                + (e.getLastName() != null ? " " + e.getLastName() : "")).trim();

        // Use Lombok builder for clean DTO creation
        return TmsClientDto.builder()
                .id(e.getId())
                .companyName((e.getCompanyName() != null && !e.getCompanyName().isBlank()) ? e.getCompanyName() : null)
                .firstName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getFirstName() : null)
                .lastName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getLastName() : null)
                .build();
    }

    /* ---------- Project: Entity → DTO ---------- */

    /**
     * Maps a {@link ProjectEntity} to a {@link TmsProjectDto}.
     * <p>
     * - Embeds a slim {@link TmsClientDto} inside the project DTO.

     * - Formats created/updated timestamps using {@link #FORMATTER}.
     *
     * @param e Project entity to map
     * @return project DTO suitable for API responses
     */
    public static TmsProjectDto toProjectDto(ProjectEntity e) {
        if (e == null) return null;

        log.debug("Mapping ProjectEntity -> TmsProjectDto (id={}, name={})", e.getId(), e.getName());

        return new TmsProjectDto(
                e.getId(),
                toClientDto(e.getClientEntity()), // embed slim client info
                e.getClientEntity() != null ? e.getClientEntity().getId() : null,
                e.getName(),
                e.getCode(),
                e.getHourlyRate(),
                e.getStartDate(),
                e.getEndDate(),
                e.getDescription(),
                e.isActive(),
                e.getCreatedDt() != null ? FORMATTER.format(e.getCreatedDt()) : null,
                e.getUpdatedDt() != null ? FORMATTER.format(e.getUpdatedDt()) : null
        );
    }

    /* ---------- Project: DTO (create) → Entity ---------- */

    /**
     * Converts an incoming {@link TmsProjectDto} into a {@link ProjectEntity}
     * when creating a new project.
     *
     * @param dto project DTO from request payload
     * @param client resolved client entity to associate with the project
     * @return new project entity ready for persistence
     */
    public static ProjectEntity fromCreateDto(TmsProjectDto dto, ClientEntity client) {
        if (dto == null) return null;

        log.debug("Mapping TmsProjectDto (create) -> ProjectEntity for clientId={}", client.getId());

        return ProjectEntity.builder()
                .clientEntity(client)
                .name(dto.projectName())
                .code(dto.code())
                .hourlyRate(dto.hourlyRate())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .description(dto.description())
                .active(Boolean.TRUE.equals(dto.isActive()))
                .build();
    }

    /* ---------- Project: DTO (update) → Entity ---------- */

    /**
     * Applies updates from {@link TmsProjectDto} into an existing {@link ProjectEntity}.
     * <p>
     * - Supports partial updates (only non-null fields are applied).

     * - If client changes, applies the new {@link ClientEntity}.
     *
     * @param target the entity being updated
     * @param dto DTO containing new values
     * @param clientIfChanged new client entity (if reassigned), else {@code null}
     */
    public static void applyUpdate(ProjectEntity target, TmsProjectDto dto, ClientEntity clientIfChanged) {
        if (dto == null || target == null) return;

        log.debug("Applying updates from TmsProjectDto -> ProjectEntity (id={})", target.getId());

        // Reassign client if changed
        if (clientIfChanged != null) {
            log.debug("Updating client for project id={}", target.getId());
            target.setClientEntity(clientIfChanged);
        }

        // Patch only non-null fields
        if (dto.projectName() != null) target.setName(dto.projectName());
        if (dto.code() != null) target.setCode(dto.code());
        if (dto.hourlyRate() != null) target.setHourlyRate(dto.hourlyRate());
        if (dto.startDate() != null) target.setStartDate(dto.startDate());
        if (dto.endDate() != null) target.setEndDate(dto.endDate());
        if (dto.description() != null) target.setDescription(dto.description());
        if (dto.isActive() != null) target.setActive(Boolean.TRUE.equals(dto.isActive()));

        log.debug("Update applied successfully for project id={}", target.getId());
    }
}
