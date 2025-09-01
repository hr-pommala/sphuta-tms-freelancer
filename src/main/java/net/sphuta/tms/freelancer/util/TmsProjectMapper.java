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
 * # TmsProjectMapper
 *
 * Centralized, stateless utility for converting between entities and DTOs.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Convert {@link ProjectEntity} → {@link TmsProjectDto} for API responses</li>
 *   <li>Convert {@link TmsProjectDto} → {@link ProjectEntity} for create/update persistence</li>
 *   <li>Convert {@link ClientEntity} → {@link TmsClientDto} for embedding client info in project DTOs</li>
 * </ul>
 *
 * <p>Design Notes:</p>
 * <ul>
 *   <li>Marked {@code final} and has a private constructor to enforce stateless utility pattern</li>
 *   <li>All methods are static to simplify usage across service layers</li>
 *   <li>Uses ISO-8601 for timestamp formatting</li>
 *   <li>Integrated with SLF4J for traceability</li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TmsProjectMapper {

    /** Formatter for timestamps returned in API (ISO-8601 string). */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /* ---------- Client ---------- */

    /**
     * Maps a {@link ClientEntity} to {@link TmsClientDto}.
     *
     * @param e client entity
     * @return mapped DTO (id + name) or null if entity is null
     */
    public static TmsClientDto toClientDto(ClientEntity e) {
        if (e == null) return null;
        log.debug("Mapping ClientEntity -> TmsClientDto (id={})", e.getId());
        return new TmsClientDto(e.getId(), e.getName());
    }

    /* ---------- Project: Entity → DTO ---------- */

    /**
     * Maps a {@link ProjectEntity} to {@link TmsProjectDto}.
     *
     * @param e project entity
     * @return mapped DTO including client, project details, and audit fields
     */
    public static TmsProjectDto toProjectDto(ProjectEntity e) {
        if (e == null) return null;

        log.debug("Mapping ProjectEntity -> TmsProjectDto (id={}, name={})", e.getId(), e.getName());

        return new TmsProjectDto(
                e.getId(),
                toClientDto(e.getClientEntity()),
                e.getClientEntity() != null ? e.getClientEntity().getId() : null,
                e.getName(),
                e.getCode(),
                e.getHourlyRate(),
                e.getStartDate(),
                e.getEndDate(),
                e.getDescription(),
                e.isActive(),
                e.getCreatedAt() != null ? FORMATTER.format(e.getCreatedAt()) : null,
                e.getUpdatedAt() != null ? FORMATTER.format(e.getUpdatedAt()) : null
        );
    }

    /* ---------- Project: DTO (create) → Entity ---------- */

    /**
     * Builds a new {@link ProjectEntity} from a {@link TmsProjectDto} for creation.
     *
     * @param dto    project DTO containing create payload
     * @param client owning client entity (FK must be resolved before call)
     * @return built project entity ready for persistence
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
     * Applies updates from a {@link TmsProjectDto} onto an existing {@link ProjectEntity}.
     * <p>Only non-null fields from the DTO are applied.</p>
     *
     * @param target         project entity to update
     * @param dto            project DTO containing new values
     * @param clientIfChanged optional new client entity if clientId is updated
     */
    public static void applyUpdate(ProjectEntity target, TmsProjectDto dto, ClientEntity clientIfChanged) {
        if (dto == null || target == null) return;

        log.debug("Applying updates from TmsProjectDto -> ProjectEntity (id={})", target.getId());

        if (clientIfChanged != null) {
            log.debug("Updating client for project id={}", target.getId());
            target.setClientEntity(clientIfChanged);
        }
        if (dto.projectName() != null) {
            target.setName(dto.projectName());
        }
        if (dto.code() != null) {
            target.setCode(dto.code());
        }
        if (dto.hourlyRate() != null) {
            target.setHourlyRate(dto.hourlyRate());
        }
        if (dto.startDate() != null) {
            target.setStartDate(dto.startDate());
        }
        if (dto.endDate() != null) {
            target.setEndDate(dto.endDate());
        }
        if (dto.description() != null) {
            target.setDescription(dto.description());
        }
        if (dto.isActive() != null) {
            target.setActive(Boolean.TRUE.equals(dto.isActive()));
        }

        log.debug("Update applied successfully for project id={}", target.getId());
    }
}
