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
import java.util.Optional;

/**
 * Implementation of {@link TmsProjectService}.
 *
 * <p>This service class is responsible for handling all business logic related to
 * {@link ProjectEntity} and {@link ClientEntity}, including:
 * <ul>
 *   <li>CRUD operations for projects</li>
 *   <li>Archiving and unarchiving projects</li>
 *   <li>Client lookup with pagination and search</li>
 *   <li>Conflict detection (duplicate name/code per client)</li>
 * </ul>
 *
 * <p>It uses Spring Data JPA repositories for persistence and transaction handling.
 * The {@link Transactional} annotation ensures proper transaction management.
 */
@Slf4j
@Service
@Transactional
public class TmsProjectServiceImpl implements TmsProjectService {

    /**
     * Standard ISO instant formatter.
     * <p>
     * Declared for consistency or future usage, currently unused in this implementation.
     */
    @SuppressWarnings("unused")
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    /** Repository for client persistence operations. */
    @Autowired
    private TmsClientRepository clients;

    /** Repository for project persistence operations. */
    @Autowired
    private TmsProjectRepository projects;

    /**
     * Retrieve a paginated list of active clients with optional search.
     *
     * @param search search keyword (name/email/company), can be null or blank
     * @param page   page index (0-based)
     * @param size   number of records per page
     * @return a {@link Page} of {@link TmsClientDto}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsClientDto> listClients(String search, int page, int size) {
        log.debug("listClients(search='{}', page={}, size={})", search, page, size);

        // Pageable configuration: sorted by companyName ascending for stable UI dropdowns
        var pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());

        // Perform search query (falls back to all active clients if search is null/blank)
        var p = clients.search(search, pageable);

        // Convert entity results into DTOs
        var result = p.map(TmsProjectMapper::toClientDto);

        // Logging pagination details
        log.info("listClients -> totalElements={}, totalPages={}, page={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }

    /**
     * Create a new project under a specific client.
     *
     * @param in DTO containing project details
     * @return created {@link TmsProjectDto}
     * @throws NotFoundException if client is not found
     * @throws ConflictException if a project with the same name/code already exists for the client
     */
    @Override
    public TmsProjectDto createProject(TmsProjectDto in) {
        log.debug("createProject requested: name='{}', code='{}', clientId='{}'",
                in.projectName(), in.code(), in.clientId());

        // Validate client existence before associating project
        var client = clients.findById(in.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Date validation (only one rule: endDate must not be before startDate)
        if (Optional.ofNullable(in.startDate()).isPresent()
                && Optional.ofNullable(in.endDate()).isPresent()
                && in.endDate().isBefore(in.startDate())) {
            throw new ConflictException("End date cannot be before start date");
        }

        // Enforce uniqueness of project name per client
        if (projects.existsByClientEntity_IdAndNameIgnoreCase(client.getId(), in.projectName())) {
            log.warn("createProject conflict: (clientId, projectName) already exists");
            throw new ConflictException("Project already exists for this client (clientId, projectName)");
        }

        // Enforce uniqueness of project code (if provided) per client
        Optional.ofNullable(in.code())
                .filter(code -> !code.isBlank())
                .ifPresent(code -> {
                    if (projects.existsByClientEntity_IdAndCodeIgnoreCase(client.getId(), code)) {
                        log.warn("createProject conflict: duplicate code per client");
                        throw new ConflictException("Duplicate code for this client");
                    }
                });

        // Build and initialize project entity with provided DTO data
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

        // Persist the project entity
        var saved = projects.save(entity);
        log.info("Created project id={} name={} client={}", saved.getId(), saved.getName(), client.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Update a project by ID.
     *
     * This method performs a full replace of the project's fields (behaves like HTTP PUT).
     *
     * @param id project identifier
     * @param in updated project DTO (all fields expected to be set for a full replace)
     * @return updated {@link TmsProjectDto}
     * @throws NotFoundException if project or client not found
     */
    @Override
    public TmsProjectDto updateProject(int id, TmsProjectDto in) {
        log.debug("updateProject(id={})", id);

        // Ensure the project exists before updating
        var project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // Resolve client entity if needed
        var client = clients.findById(in.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Delegate field updates to mapper
        TmsProjectMapper.applyUpdate(project, in, client);

        // Date validation (same rule as create)
        if (Optional.ofNullable(project.getStartDate()).isPresent()
                && Optional.ofNullable(project.getEndDate()).isPresent()
                && project.getEndDate().isBefore(project.getStartDate())) {
            log.warn("updateProject conflict: endDate is before startDate for project id={}", id);
            throw new ConflictException("End date cannot be before start date");
        }

        // Persist updated project
        var saved = projects.save(project);
        log.info("Project updated: id={}", saved.getId());
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Archive or unarchive a project.
     *
     * @param id        project identifier
     * @param unarchive true to unarchive (set active), false to archive (set inactive)
     * @return updated {@link TmsProjectDto}
     * @throws NotFoundException if project not found
     * @throws ConflictException if requested state is already applied
     */
    @Override
    public TmsProjectDto archiveProject(int id, boolean unarchive) {
        log.debug("archiveProject(id={}, unarchive={})", id, unarchive);

        var p = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // Prevent redundant state changes
        if (unarchive && p.isActive()) throw new ConflictException("Project already active");
        if (!unarchive && !p.isActive()) throw new ConflictException("Project already archived");

        // Apply state change
        p.setActive(unarchive);
        var saved = projects.save(p);
        log.info("{} project id={}", unarchive ? "Unarchived" : "Archived", id);
        return TmsProjectMapper.toProjectDto(saved);
    }

    /**
     * Permanently delete a project by ID.
     *
     * @param id project identifier
     * @throws NotFoundException if project does not exist
     */
    @Override
    public void deleteProject(int id) {
        log.debug("deleteProject(id={}) requested", id);

        // Ensure project exists before deletion
        var project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // Delete project
        projects.delete(project);
        log.info("Project deleted: id={}", id);
    }

    /**
     * List projects with filters and pagination.
     *
     * @param active   true = active projects only, false = archived
     * @param clientId optional filter by client ID
     * @param search   optional search term (applies to project name/code/etc.)
     * @param page     page index (0-based)
     * @param size     number of records per page
     * @return a {@link Page} of {@link TmsProjectDto}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TmsProjectDto> listProjects(boolean active, Integer clientId, String search, int page, int size) {
        log.debug("listProjects(active={}, clientId={}, search='{}', page={}, size={})",
                active, clientId, search, page, size);

        // Pageable sorted by project name
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Apply filters with Optional to avoid null checks
        Page<ProjectEntity> p =
                Optional.ofNullable(clientId).map(cid ->
                        Optional.ofNullable(search)
                                .filter(s -> !s.isBlank())
                                .map(s -> projects.searchByActiveAndClientAndTerm(active, cid, s, pageable))
                                .orElseGet(() -> projects.findByActiveAndClientEntity_Id(active, cid, pageable))
                ).orElseGet(() ->
                        Optional.ofNullable(search)
                                .filter(s -> !s.isBlank())
                                .map(s -> projects.searchByActiveAndTerm(active, s, pageable))
                                .orElseGet(() -> projects.findByActive(active, pageable))
                );

        // Map to DTOs
        var result = p.map(TmsProjectMapper::toProjectDto);
        log.info("listProjects -> totalElements={}, totalPages={}, page={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber());
        return result;
    }


    /* ------------ helpers ------------ */

    /**
     * Parse a sort string (e.g., "field,asc" or "field,desc") into {@link Sort.Order}.
     *
     * @param s sort expression
     * @return corresponding {@link Sort.Order}
     */
    private Sort.Order parseSort(String s) {
        var parts = s.split(",", 2);
        var prop = parts[0];
        var dir = (parts.length == 2 ? parts[1] : "asc").toLowerCase();
        return "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
    }

    /**
     * Utility to return the first non-null value between two inputs.
     *
     * @param a   first candidate value
     * @param b   fallback value
     * @param <T> generic type
     * @return {@code a} if not null, otherwise {@code b}
     */
    private static <T> T coalesce(T a, T b) {
        return a != null ? a : b;
    }
}
