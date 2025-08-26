package net.sphuta.tms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.dto.*;
import net.sphuta.tms.entity.Client;
import net.sphuta.tms.entity.Project;
import net.sphuta.tms.exception.NotFoundException;
import net.sphuta.tms.exception.ConflictException;
import net.sphuta.tms.repository.ClientRepository;
import net.sphuta.tms.repository.ProjectRepository;
import net.sphuta.tms.service.TmsService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service-layer implementation for Clients & Projects use-cases.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Client listing for Owner dropdown</li>
 *   <li>Project create/update/archive/unarchive/delete</li>
 *   <li>Project listings (active, archived, active-by-owner+search)</li>
 * </ul>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Transaction boundaries are declared at class level; read-only methods override as needed.</li>
 *   <li>Application-level uniqueness checks are performed prior to persistence to produce clean messages.
 *       Database constraints still enforce integrity.</li>
 *   <li>Added structured logging at method entry/exit and key branches, without altering behavior.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TmsServiceImpl implements TmsService {

    private final ClientRepository clients;
    private final ProjectRepository projects;

    /** ISO-8601 instant formatter for view-model timestamps. */
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    /**
     * List active clients with optional name search.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> listClients(String search, int page, int size) {
        log.debug("listClients(search='{}', page={}, size={})", search, page, size);

        // Pagination: sort by name ASC for stable dropdowns.
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Conditional search vs full active list.
        var p = (search!=null && !search.isBlank())
                ? clients.findByActiveTrueAndNameContainingIgnoreCase(search, pageable)
                : clients.findByActiveTrue(pageable);

        // Map entity -> dto (only id + name for slim dropdown projection).
        var result = p.map(c -> ClientDto.builder().id(c.getId().toString()).name(c.getName()).build());

        log.info("listClients -> totalElements={}, totalPages={}, page={}", result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }

    /**
     * Create a new project after validating client existence and uniqueness constraints.
     */
    @Override
    public ProjectDtos.View createProject(ProjectDtos.Create in) {
        log.debug("createProject requested: name='{}', code='{}', clientId='{}'", in.getProjectName(), in.getCode(), in.getClientId());

        // FK must exist.
        var client = clients.findById(UUID.fromString(in.getClientId()))
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Uniqueness (app-level check; DB also enforces).
        if (projects.existsByClient_IdAndNameIgnoreCase(client.getId(), in.getProjectName())) {
            log.warn("createProject conflict: (clientId, projectName) already exists");
            throw new ConflictException("Project already exists for this client (clientId, projectName)");
        }

        if (in.getCode()!=null && !in.getCode().isBlank() &&
                projects.existsByClient_IdAndCodeIgnoreCase(client.getId(), in.getCode())) {
            log.warn("createProject conflict: duplicate code per client");
            throw new ConflictException("Duplicate code for this client");
        }

        // Build entity from DTO.
        var entity = Project.builder()
                .client(client)
                .name(in.getProjectName())
                .code(in.getCode())
                .hourlyRate(in.getHourlyRate())
                .startDate(in.getStartDate())
                .endDate(in.getEndDate())
                .description(in.getDescription())
                .active(Boolean.TRUE.equals(in.getIsActive()))
                .build();

        // Persist and map to view.
        var saved = projects.save(entity);
        log.info("Created project id={} name={} client={}", saved.getId(), saved.getName(), client.getId());
        return toView(saved);
    }

    /**
     * Update a project. Supports both PUT (fullReplace=true) and PATCH (fullReplace=false).
     */
    @Override
    public ProjectDtos.View updateProject(UUID id, ProjectDtos.Update in, boolean fullReplace) {
        log.debug("updateProject(id={}, fullReplace={})", id, fullReplace);

        // Load target project.
        var project = projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));

        // Re-parent if clientId supplied (only when provided for PATCH; required on PUT).
        if (in.getClientId()!=null) {
            var newClient = clients.findById(UUID.fromString(in.getClientId()))
                    .orElseThrow(() -> new NotFoundException("Client not found"));
            project.setClient(newClient);
            log.debug("updateProject: re-parented to client={}", newClient.getId());
        } else if (fullReplace) {
            // PUT requires clientId in DTO (caller contract).
            log.warn("updateProject: PUT without clientId");
            throw new IllegalArgumentException("clientId is required for PUT");
        }

        // Name updates + uniqueness when name changes or client changed.
        if (in.getProjectName()!=null || fullReplace) {
            var newName = coalesce(in.getProjectName(), fullReplace ? "" : project.getName());
            if (newName==null || newName.isBlank())
                throw new IllegalArgumentException("projectName must not be blank");

            // If name changed or client changed, ensure uniqueness in (client, name).
            if (!newName.equalsIgnoreCase(project.getName()) || (in.getClientId()!=null)) {
                if (projects.existsByClient_IdAndNameIgnoreCase(project.getClient().getId(), newName)) {
                    log.warn("updateProject conflict: (clientId, projectName) already exists");
                    throw new ConflictException("Project already exists for this client (clientId, projectName)");
                }
            }
            project.setName(newName);
        }

        // Code updates + uniqueness per client if provided.
        if (in.getCode()!=null || fullReplace) {
            var newCode = coalesce(in.getCode(), null);
            if (newCode!=null && projects.existsByClient_IdAndCodeIgnoreCase(project.getClient().getId(), newCode)
                    && (project.getCode()==null || !project.getCode().equalsIgnoreCase(newCode))) {
                log.warn("updateProject conflict: duplicate code per client");
                throw new ConflictException("Duplicate code for this client");
            }
            project.setCode(newCode);
        }

        // Hourly rate updates (validate > 0 when provided).
        if (in.getHourlyRate()!=null || fullReplace) {
            if (in.getHourlyRate()==null) throw new IllegalArgumentException("hourlyRate must be provided");
            if (in.getHourlyRate().signum()<=0) throw new IllegalArgumentException("hourlyRate must be > 0");
            project.setHourlyRate(in.getHourlyRate());
        }

        // Date updates + cross-field check (end >= start) when both present.
        if (in.getStartDate()!=null || fullReplace) project.setStartDate(in.getStartDate());
        if (in.getEndDate()!=null || fullReplace) project.setEndDate(in.getEndDate());
        if (project.getStartDate()!=null && project.getEndDate()!=null &&
                project.getEndDate().isBefore(project.getStartDate())) {
            log.warn("updateProject validation: endDate before startDate");
            throw new IllegalArgumentException("endDate must be on/after startDate");
        }

        // Description & active flag (respect PATCH semantics).
        if (in.getDescription()!=null || fullReplace) project.setDescription(in.getDescription());
        if (in.getIsActive()!=null || fullReplace) project.setActive(Boolean.TRUE.equals(in.getIsActive()));

        // Save changes.
        var saved = projects.save(project);
        log.info("Updated project id={} (fullReplace={})", saved.getId(), fullReplace);
        return toView(saved);
    }

    /**
     * Archive or unarchive a project (toggle active flag).
     */
    @Override
    public ProjectDtos.View archiveProject(UUID id, boolean unarchive) {
        log.debug("archiveProject(id={}, unarchive={})", id, unarchive);

        var p = projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        if (unarchive && p.isActive())
            throw new ConflictException("Project already active");
        if (!unarchive && !p.isActive())
            throw new ConflictException("Project already archived");

        p.setActive(unarchive);
        var saved = projects.save(p);
        log.info("{} project id={}", unarchive ? "Unarchived" : "Archived", id);
        return toView(saved);
    }

    /**
     * Hard delete a project by ID.
     */
    @Override
    public void deleteProject(UUID id) {
        log.warn("deleteProject(id={}) requested", id);

        if (!projects.existsById(id)) throw new NotFoundException("Project not found");
        projects.deleteById(id);

        log.info("deleteProject(id={}) completed", id);
    }

    /**
     * List active projects (active=true), paginated.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDtos.View> listActiveProjects(int page, int size) {
        log.debug("listActiveProjects(page={}, size={})", page, size);

        var pageable = PageRequest.of(page, size); // no explicit sort per your requirement
        var p = projects.findAllByActiveAndNameContainingIgnoreCase(true, "", pageable);
        var result = p.map(this::toView);

        log.info("listActiveProjects -> totalElements={}, page={}", result.getTotalElements(), result.getNumber());
        return result;
    }

    /**
     * List archived projects (active=false), paginated.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDtos.View> listArchivedProjects(int page, int size) {
        log.debug("listArchivedProjects(page={}, size={})", page, size);

        var pageable = PageRequest.of(page, size);
        var p = projects.findAllByActiveAndNameContainingIgnoreCase(false, "", pageable);
        var result = p.map(this::toView);

        log.info("listArchivedProjects -> totalElements={}, page={}", result.getTotalElements(), result.getNumber());
        return result;
    }

    /**
     * List active projects filtered by Owner (clientId) and name search.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDtos.View> listActiveProjectsByOwnerAndSearch(String clientId, String search, int page, int size) {
        log.debug("listActiveProjectsByOwnerAndSearch(clientId={}, search='{}', page={}, size={})",
                clientId, search, page, size);

        var pageable = PageRequest.of(page, size);
        var uuid = UUID.fromString(clientId);
        var q = (search == null) ? "" : search;

        var p = projects.findAllByActiveAndClient_IdAndNameContainingIgnoreCase(true, uuid, q, pageable);
        var result = p.map(this::toView);

        log.info("listActiveProjectsByOwnerAndSearch -> totalElements={}, page={}", result.getTotalElements(), result.getNumber());
        return result;
    }


    /* ------------ helpers ------------ */

    /**
     * Parses a single "prop,dir" sort token into a {@link Sort.Order}.
     * <p>Note: retained for compatibility; not used by current list endpoints.</p>
     */
    private Sort.Order parseSort(String s) {
        var parts = s.split(",", 2);
        var prop = parts[0];
        var dir = (parts.length==2 ? parts[1] : "asc").toLowerCase();
        return "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
    }

    /**
     * Maps a {@link Project} entity to {@link ProjectDtos.View}.
     * Includes a slim nested {@link ClientDto} for the owner.
     */
    private ProjectDtos.View toView(Project p) {
        var clientView = ClientDto.builder().id(p.getClient().getId().toString()).name(p.getClient().getName()).build();
        return ProjectDtos.View.builder()
                .id(p.getId().toString())
                .client(clientView)
                .projectName(p.getName())
                .code(p.getCode())
                .hourlyRate(p.getHourlyRate())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .description(p.getDescription())
                .isActive(p.isActive())
                .createdAt(p.getCreatedAt()!=null ? ISO_INSTANT.format(p.getCreatedAt()) : null)
                .updatedAt(p.getUpdatedAt()!=null ? ISO_INSTANT.format(p.getUpdatedAt()) : null)
                .build();
    }

    /** Null-coalescing helper; returns {@code a} if non-null, else {@code b}. */
    private static <T> T coalesce(T a, T b) { return a!=null ? a : b; }
}
