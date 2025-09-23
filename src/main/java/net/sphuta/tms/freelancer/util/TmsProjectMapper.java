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
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TmsProjectMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /* ---------- Client Mapping (dropdown/slim) ---------- */
    public static TmsClientDto toClientDto(ClientEntity e) {
        if (e == null) return null;
        log.debug("Mapping ClientEntity -> slim TmsClientDto (id={})", e.getId());

        String displayName = (e.getCompanyName() != null && !e.getCompanyName().isBlank())
                ? e.getCompanyName()
                : ((e.getFirstName() != null ? e.getFirstName() : "")
                + (e.getLastName() != null ? " " + e.getLastName() : "")).trim();

        return TmsClientDto.builder()
                .id(e.getId())
                .companyName((e.getCompanyName() != null && !e.getCompanyName().isBlank()) ? e.getCompanyName() : null)
                .firstName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getFirstName() : null)
                .lastName((e.getCompanyName() == null || e.getCompanyName().isBlank()) ? e.getLastName() : null)
                .build();
    }

    /* ---------- Project: Entity -> DTO (with slim client) ---------- */
    public static TmsProjectDto toProjectDto(ProjectEntity e) {
        if (e == null) return null;

        log.debug("Mapping ProjectEntity -> TmsProjectDto (id={}, name={})", e.getId(), e.getName());

        // ✅ Slim client: only id, userId, and companyName
        TmsClientDto slimClientDto = null;
        if (e.getClientEntity() != null) {
            slimClientDto = TmsClientDto.builder()
                    .id(e.getClientEntity().getId())
                    .userId(e.getClientEntity().getUserId())
                    .companyName(e.getClientEntity().getCompanyName())
                    .build();
        }

        return new TmsProjectDto(
                e.getId(),
                slimClientDto,                           // embed slim client info
                e.getUserId(),                           // project-level userId
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

    /* ---------- Project: DTO (create) -> Entity ---------- */
    public static ProjectEntity fromCreateDto(TmsProjectDto dto, ClientEntity client) {
        if (dto == null) return null;

        log.debug("Mapping TmsProjectDto (create) -> ProjectEntity for clientId={}",
                client == null ? "null" : client.getId());

        ProjectEntity p = ProjectEntity.builder()
                .clientEntity(client)
                .name(dto.projectName())
                .code(dto.code())
                .hourlyRate(dto.hourlyRate())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .description(dto.description())
                .active(Boolean.TRUE.equals(dto.isActive()))
                .build();

        // copy owner userId from client if available
        if (client != null) {
            p.setUserId(client.getUserId());
        }

        return p;
    }

    /* ---------- Project: DTO (update) -> Entity (PUT semantics) ---------- */
    public static void applyUpdate(ProjectEntity target, TmsProjectDto dto, ClientEntity clientIfChanged) {
        if (dto == null || target == null) return;

        log.debug("Applying full update from TmsProjectDto -> ProjectEntity (id={})", target.getId());

        if (clientIfChanged != null) {
            log.debug("Setting new client for project id={}", target.getId());
            target.setClientEntity(clientIfChanged);
            // keep project.userId in sync with the client
            target.setUserId(clientIfChanged.getUserId());
        }

        target.setName(dto.projectName());
        target.setCode(dto.code());
        target.setHourlyRate(dto.hourlyRate());
        target.setStartDate(dto.startDate());
        target.setEndDate(dto.endDate());
        target.setDescription(dto.description());
        target.setActive(Boolean.TRUE.equals(dto.isActive()));

        log.debug("Full update applied successfully for project id={}", target.getId());
    }
}
