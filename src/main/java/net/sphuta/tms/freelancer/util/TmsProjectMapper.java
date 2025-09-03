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
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TmsProjectMapper {

    /** Formatter for timestamps returned in API (ISO-8601 string). */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /* ---------- Client ---------- */

    /**
     * Maps a {@link ClientEntity} to {@link TmsClientDto}.
     * Uses the slim DTO factory for dropdowns and lightweight responses.
     */
    public static TmsClientDto toClientDto(ClientEntity e) {
        if (e == null) return null;
        log.debug("Mapping ClientEntity -> TmsClientDto (id={})", e.getId());

        // Build display name: prefer companyName, else combine first + last name
        String displayName = (e.getCompanyName() != null && !e.getCompanyName().isBlank())
                ? e.getCompanyName()
                : ((e.getFirstName() != null ? e.getFirstName() : "")
                + (e.getLastName() != null ? " " + e.getLastName() : "")).trim();

        // Use builder to create slim DTO with only id and display name
        return TmsClientDto.builder()
                .id(e.getId())
                .companyName((e.getCompanyName() != null && !e.getCompanyName().isBlank()) ? e.getCompanyName() : null)
                .firstName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getFirstName() : null)
                .lastName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getLastName() : null)
                .build();
    }

    /* ---------- Project: Entity → DTO ---------- */

    public static TmsProjectDto toProjectDto(ProjectEntity e) {
        if (e == null) return null;

        log.debug("Mapping ProjectEntity -> TmsProjectDto (id={}, name={})", e.getId(), e.getName());

        return new TmsProjectDto(
                e.getId(),
                toClientDto(e.getClientEntity()), // embed slim client
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

    public static void applyUpdate(ProjectEntity target, TmsProjectDto dto, ClientEntity clientIfChanged) {
        if (dto == null || target == null) return;

        log.debug("Applying updates from TmsProjectDto -> ProjectEntity (id={})", target.getId());

        if (clientIfChanged != null) {
            log.debug("Updating client for project id={}", target.getId());
            target.setClientEntity(clientIfChanged);
        }
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
