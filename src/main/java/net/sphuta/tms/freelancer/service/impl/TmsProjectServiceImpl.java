package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import net.sphuta.tms.freelancer.service.TmsProjectService;
import net.sphuta.tms.freelancer.util.TmsProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

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
@Transactional
public class TmsProjectServiceImpl implements TmsProjectService {

    /** ISO-8601 instant formatter for view-model timestamps. (kept for reference) */
    @SuppressWarnings("unused")
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    // ---- Dependencies (field injection per your instruction to use @Autowired) ----
    @Autowired
    private TmsClientRepository clients;

    @Autowired
    private TmsProjectRepository projects;

    /**
     * List active clients with optional name search.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsClientDto> listClients(String search, int page, int size) {
        log.debug("listClients(search='{}', page={}, size={})", search, page, size);

        // Pagination: sort by name ASC for stable dropdowns.
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Conditional search vs full active list.
        var p = (search != null && !search.isBlank())
                ? clients.findByActiveTrueAndNameContainingIgnoreCase(search, pageable)
                : clients.findByActiveTrue(pageable);

        // Map entity -> dto (only id + name for slim dropdown projection).
        var result = p.map(TmsProjectMapper::toClientDto);

        log.info("listClients -> totalElements={}, totalPages={}, page={}", result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }

    /**
     * Create a new project after validating client existence and uniqueness constraints.
     */
    @Override
    public TmsProjectDto createProject(TmsProjectDto in) {
        log.debug("createProject requested: name='{}', code='{}', clientId='{}'",
                in.projectName(), in.code(), in.clientId());

        // FK must exist.
        var client = clients.findById(in.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Uniqueness (app-level check; DB also enforces).
        if (projects.existsByClientEntity_IdAndNameIgnoreCase(client.getId(), in.projectName())) {
            log.warn("createProject conflict: (clientId, projectName) already exists");
            throw new ConflictException("Project already exists for this client (clientId, projectName)");
        }

        if (in.code() != null && !in.code().isBlank()
                && projects.existsByClientEntity_IdAndCodeIgnoreCase(client.getId(), in.code())) {
            log.warn("createProject conflict: duplicate code per client");
            throw new ConflictException("Duplicate code for this client");
        }

        // Build entity from DTO.
        var entity = ProjectEntity.builder()
                .clientEntity(client)
                .name(in.projectName())
                .code(in.code())
                .hourlyRate(in.hourlyRate())
                .startDate(in.startDate())
                .endDate(in.endDate())
                .description(in.description())
                .active(Boolean.TRUE.equals(in.isActive()))
                .build();

        // Persist and map to dto.
        var saved = projects.save(entity);
        log.info("Created project id={} name={} client={}", saved.getId(), saved.getName(), client.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Update a project. Supports both PUT (fullReplace=true) and PATCH (fullReplace=false).
     */
    @Override
    public TmsProjectDto updateProject(int id, TmsProjectDto in, boolean fullReplace) {
        log.debug("updateProject(id={}, fullReplace={})", id, fullReplace);

        // Load target project
        var project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // --- PUT (full replace) ---
        if (fullReplace) {
            project.setClientEntity(
                    clients.findById(in.clientId())
                            .orElseThrow(() -> new NotFoundException("Client not found"))
            );
            project.setName(in.projectName());
            project.setCode(in.code());
            project.setHourlyRate(in.hourlyRate());
            project.setStartDate(in.startDate());
            project.setEndDate(in.endDate());
            project.setDescription(in.description());
            project.setActive(Boolean.TRUE.equals(in.isActive()));
        }
        // --- PATCH (partial update) ---
        else {
            if (in.clientId() != null) {
                var client = clients.findById(in.clientId())
                        .orElseThrow(() -> new NotFoundException("Client not found"));
                project.setClientEntity(client);
            }
            if (in.projectName() != null) project.setName(in.projectName());
            if (in.code() != null) project.setCode(in.code());
            if (in.hourlyRate() != null) project.setHourlyRate(in.hourlyRate());
            if (in.startDate() != null) project.setStartDate(in.startDate());
            if (in.endDate() != null) project.setEndDate(in.endDate());
            if (in.description() != null) project.setDescription(in.description());
            if (in.isActive() != null) project.setActive(in.isActive());
        }

        // Save changes
        var saved = projects.save(project);

        log.info("Project {} updated: id={}", fullReplace ? "fully" : "partially", saved.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }


    /**
     * Archive or unarchive a project (toggle active flag).
     */
    @Override
    public TmsProjectDto archiveProject(int id, boolean unarchive) {
        log.debug("archiveProject(id={}, unarchive={})", id, unarchive);

        var p = projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        if (unarchive && p.isActive())
            throw new ConflictException("Project already active");
        if (!unarchive && !p.isActive())
            throw new ConflictException("Project already archived");

        p.setActive(unarchive);
        var saved = projects.save(p);
        log.info("{} project id={}", unarchive ? "Unarchived" : "Archived", id);
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Delete a project permanently by ID.
     */
    @Override
    public void deleteProject(int id) {
        log.debug("deleteProject(id={}) requested", id);

        var project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projects.delete(project);
        log.info("Project deleted: id={}", id);
    }

    /**
     * List projects with filters (active, archived, client, search).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsProjectDto> listProjects(boolean active, Integer clientId, String search, int page, int size) {
        log.debug("listProjects(active={}, clientId={}, search='{}', page={}, size={})",
                active, clientId, search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProjectEntity> p;

        if (clientId != null && search != null && !search.isBlank()) {
            // filter by active, client, search
            p = projects.findByActiveAndClientEntity_IdAndNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    active, clientId, search, search, search, pageable);
        } else if (clientId != null) {
            // filter by active + client
            p = projects.findByActiveAndClientEntity_Id(active, clientId, pageable);
        } else if (search != null && !search.isBlank()) {
            // filter by active + search
            p = projects.findByActiveAndNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    active, search, search, search, pageable);
        } else {
            // only active flag
            p = projects.findByActive(active, pageable);
        }

        var result = p.map(TmsProjectMapper::toProjectDto);

        log.info("listProjects -> totalElements={}, totalPages={}, page={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber());

        return result;
    }



    /* ------------ helpers ------------ */

    /**
     * Parses a single "prop,dir" sort token into a {@link Sort.Order}.
     * <p>Note: retained for compatibility; not used by current list endpoints.</p>
     */
    @SuppressWarnings("unused")
    private Sort.Order parseSort(String s) {
        var parts = s.split(",", 2);
        var prop = parts[0];
        var dir = (parts.length == 2 ? parts[1] : "asc").toLowerCase();
        return "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
    }

    /** Null-coalescing helper; returns {@code a} if non-null, else {@code b}. */
    private static <T> T coalesce(T a, T b) { return a != null ? a : b; }
}
