package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import net.sphuta.tms.freelancer.service.TmsProjectService;
import net.sphuta.tms.freelancer.util.TmsProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Service-layer implementation for Clients & Projects use-cases.
 */
@Slf4j
@Service
@Transactional
public class TmsProjectServiceImpl implements TmsProjectService {

    @SuppressWarnings("unused")
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    private TmsClientRepository clients;

    @Autowired
    private TmsProjectRepository projects;

    /**
     * List active clients with optional name/email/company search.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsClientDto> listClients(String search, int page, int size) {
        log.debug("listClients(search='{}', page={}, size={})", search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());

        Page<ClientEntity> p = clients.search(search, pageable);

        var result = p.map(TmsProjectMapper::toClientDto);

        log.info("listClients -> totalElements={}, totalPages={}, page={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }

    /**
     * Create a new project after validating client existence and uniqueness constraints.
     */
    @Override
    public TmsProjectDto createProject(TmsProjectDto in) {
        log.debug("createProject requested: name='{}', code='{}', clientId='{}'",
                in.projectName(), in.code(), in.clientId());

        var client = clients.findById(in.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (projects.existsByClientEntity_IdAndNameIgnoreCase(client.getId(), in.projectName())) {
            log.warn("createProject conflict: (clientId, projectName) already exists");
            throw new ConflictException("Project already exists for this client (clientId, projectName)");
        }

        if (in.code() != null && !in.code().isBlank()
                && projects.existsByClientEntity_IdAndCodeIgnoreCase(client.getId(), in.code())) {
            log.warn("createProject conflict: duplicate code per client");
            throw new ConflictException("Duplicate code for this client");
        }

        ProjectEntity entity = ProjectEntity.builder()
                .clientEntity(client)
                .name(in.projectName())
                .code(in.code())
                .hourlyRate(in.hourlyRate())
                .startDate(in.startDate())
                .endDate(in.endDate())
                .description(in.description())
                .active(Boolean.TRUE.equals(in.isActive()))
                .build();

        ProjectEntity saved = projects.save(entity);
        log.info("Created project id={} name={} client={}", saved.getId(), saved.getName(), client.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Update a project. Supports both PUT and PATCH.
     */
    @Override
    public TmsProjectDto updateProject(int id, TmsProjectDto in, boolean fullReplace) {
        log.debug("updateProject(id={}, fullReplace={})", id, fullReplace);

        ProjectEntity project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (fullReplace) {
            project.setClientEntity(clients.findById(in.clientId())
                    .orElseThrow(() -> new NotFoundException("Client not found")));
            project.setName(in.projectName());
            project.setCode(in.code());
            project.setHourlyRate(in.hourlyRate());
            project.setStartDate(in.startDate());
            project.setEndDate(in.endDate());
            project.setDescription(in.description());
            project.setActive(Boolean.TRUE.equals(in.isActive()));
        } else {
            if (in.clientId() != null) {
                project.setClientEntity(clients.findById(in.clientId())
                        .orElseThrow(() -> new NotFoundException("Client not found")));
            }
            if (in.projectName() != null) project.setName(in.projectName());
            if (in.code() != null) project.setCode(in.code());
            if (in.hourlyRate() != null) project.setHourlyRate(in.hourlyRate());
            if (in.startDate() != null) project.setStartDate(in.startDate());
            if (in.endDate() != null) project.setEndDate(in.endDate());
            if (in.description() != null) project.setDescription(in.description());
            if (in.isActive() != null) project.setActive(in.isActive());
        }

        ProjectEntity saved = projects.save(project);
        log.info("Project {} updated: id={}", fullReplace ? "fully" : "partially", saved.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Archive or unarchive a project.
     */
    @Override
    public TmsProjectDto archiveProject(int id, boolean unarchive) {
        log.debug("archiveProject(id={}, unarchive={})", id, unarchive);

        ProjectEntity p = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (unarchive && p.isActive()) throw new ConflictException("Project already active");
        if (!unarchive && !p.isActive()) throw new ConflictException("Project already archived");

        p.setActive(unarchive);
        ProjectEntity saved = projects.save(p);
        log.info("{} project id={}", unarchive ? "Unarchived" : "Archived", id);
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Delete a project permanently by ID.
     */
    @Override
    public void deleteProject(int id) {
        log.debug("deleteProject(id={}) requested", id);

        ProjectEntity project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projects.delete(project);
        log.info("Project deleted: id={}", id);
    }

    /**
     * List projects with filters.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsProjectDto> listProjects(boolean active, Integer clientId, String search, int page, int size) {
        log.debug("listProjects(active={}, clientId={}, search='{}', page={}, size={})",
                active, clientId, search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProjectEntity> p;
        if (clientId != null && search != null && !search.isBlank()) {
            p = projects.searchByActiveAndClientAndTerm(active, clientId, search, pageable);
        } else if (clientId != null) {
            p = projects.findByActiveAndClientEntity_Id(active, clientId, pageable);
        } else if (search != null && !search.isBlank()) {
            p = projects.searchByActiveAndTerm(active, search, pageable);
        } else {
            p = projects.findByActive(active, pageable);
        }

        var result = p.map(TmsProjectMapper::toProjectDto);
        log.info("listProjects -> totalElements={}, totalPages={}, page={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }

    /* ------------ helpers ------------ */
    private Sort.Order parseSort(String s) {
        var parts = s.split(",", 2);
        var prop = parts[0];
        var dir = (parts.length == 2 ? parts[1] : "asc").toLowerCase();
        return "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
    }

    private static <T> T coalesce(T a, T b) {
        return a != null ? a : b;
    }
}
