package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.ApiMessageConstants;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.response.TmsPageResponse;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.TmsProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * # TmsProjectController
 *
 * REST controller that manages **Clients** (for dropdowns/owners)
 * and **Projects CRUD** (create, read, update, archive/unarchive, delete).
 *
 * <p><b>Key Details:</b></p>
 * <ul>
 *   <li>Base path: <code>/api/v1/projects</li>
 *   <li>All responses are wrapped in {@link TmsApiResponse} for consistency.</li>
 *   <li>Pagination responses use {@link TmsPageResponse} with metadata.</li>
 *   <li>SLF4J logging included for traceability and debugging.</li>
 *   <li>Swagger/OpenAPI annotations used for API documentation.</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@Validated
public class TmsProjectController {

    /**
     * Service layer dependency to handle business logic.
     * Injected automatically by Spring.
     */
    @Autowired
    private TmsProjectService service;

    /* ============================================================
     * Section: Clients (used in Owner dropdowns for project creation)
     * ============================================================
     */

    /**
     * Returns a paginated list of clients for the Owner dropdown.
     * Can be filtered by active status and search term.
     *
     * @param active filter flag (true to fetch only active clients)
     * @param search optional case-insensitive name search
     * @param page   0-based page index
     * @param size   page size
     * @return standardized response with client DTOs + pagination metadata
     */
    @Operation(
            summary = "Clients list for Owner dropdown",
            description = "Fetches a paginated list of active clients, optionally filtered by search term."
    )
    @GetMapping("/clients")
    public TmsApiResponse<TmsPageResponse<TmsClientDto>> listClients(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        log.debug("GET /clients called: active={}, search='{}', page={}, size={}", active, search, page, size);

        // Delegate to service for paginated client retrieval
        var p = service.listClients(search, page, size);

        // Build page metadata (page number, size, total elements, total pages)
        var meta = new TmsPageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());

        // Wrap result in standardized API response
        var resp = TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.CLIENTS_FETCHED, new TmsPageResponse<>(p.getContent(), meta));

        log.info("Clients list returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /* ============================================================
     * Section: Projects CRUD (Create, Read, Update, Archive, Delete)
     * ============================================================
     */

    /**
     * Lists projects with filters such as active/archived, by client, and search.
     *
     * @param active   filter flag (true=active, false=archived)
     * @param clientId optional filter by client ID (owner)
     * @param search   optional case-insensitive search on project fields
     * @param page     0-based page index
     * @param size     page size
     * @return standardized response with project DTOs + pagination metadata
     */
    @Operation(
            summary = "List projects with filters",
            description = "Retrieves a paginated list of projects filtered by active/archived, client, and search criteria."
    )
    @GetMapping("/projects")
    public TmsApiResponse<TmsPageResponse<TmsProjectDto>> listProjects(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) Integer clientId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        log.debug("GET /projects called: active={}, clientId={}, search='{}', page={}, size={}", active, clientId, search, page, size);

        // Delegate to service for project retrieval
        var p = service.listProjects(active, clientId, search, page, size);

        // Page metadata construction
        var meta = new TmsPageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());

        // Wrap result in API response
        var resp = TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECTS_FETCHED_SUCCESS, new TmsPageResponse<>(p.getContent(), meta));

        log.info("Projects list returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /**
     * Creates a new project under a client.
     *
     * @param in validated project DTO for creation
     * @return ResponseEntity with 201 Created status, Location header, and project DTO
     */
    @Operation(
            summary = "Create project",
            description = "Creates a new project under a client and returns the created resource with location header."
    )
    @PostMapping("/projects")
    public ResponseEntity<TmsApiResponse<TmsProjectDto>> create(
            @Validated(TmsProjectDto.Create.class) @RequestBody TmsProjectDto in) {

        log.debug("POST /projects create requested for name='{}', code='{}'", in.projectName(), in.code());

        // Create project via service
        var created = service.createProject(in);

        // Build resource URI for Location header
        var location = URI.create("/api/v1/projects/" + created.id());

        // Wrap result in standardized API response
        var resp = TmsApiResponse.success(HttpStatus.CREATED, ApiMessageConstants.PROJECT_CREATED, created);

        log.info("Project created: id={}, location={}", created.id(), location);

        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Performs full update (PUT) of a project by ID.
     *
     * @param id project identifier
     * @param in validated project DTO containing all updated values
     * @return standardized response with updated project DTO
     */
    @Operation(
            summary = "Full update (PUT)",
            description = "Replaces an existing project with all new field values."
    )
    @PutMapping("/projects/{id}")
    public TmsApiResponse<TmsProjectDto> put(
            @PathVariable int id,
            @Validated(TmsProjectDto.Update.class) @RequestBody TmsProjectDto in) {

        log.debug("PUT /projects/{} full update requested", id);

        // Delegate to service with full update flag
        var updated = service.updateProject(id, in, true);

        log.info("Project fully updated: id={}", id);

        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UPDATED, updated);
    }

    /**
     * Performs partial update (PATCH) of a project by ID.
     *
     * @param id project identifier
     * @param in project DTO with only fields to update
     * @return standardized response with updated project DTO
     */
    @Operation(
            summary = "Partial update (PATCH)",
            description = "Updates only specific fields of a project without replacing the entire resource."
    )
    @PatchMapping("/projects/{id}")
    public TmsApiResponse<TmsProjectDto> patch(
            @PathVariable int id,
            @RequestBody TmsProjectDto in) {

        log.debug("PATCH /projects/{} partial update requested", id);

        // Delegate to service with partial update flag
        var updated = service.updateProject(id, in, false);

        log.info("Project partially updated: id={}", id);

        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UPDATED, updated);
    }

    /**
     * Archives a project (sets active=false).
     *
     * @param id project identifier
     * @return standardized response with archived project DTO
     */
    @Operation(
            summary = "Archive project",
            description = "Archives a project by marking it as inactive."
    )
    @PostMapping("/projects/{id}/archive")
    public TmsApiResponse<TmsProjectDto> archive(@PathVariable int id) {

        log.debug("POST /projects/{}/archive called", id);

        // Archive via service (set active=false)
        var archived = service.archiveProject(id, false);

        log.info("Project archived: id={}", id);

        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_ARCHIVED, archived);
    }

    /**
     * Unarchives a project (sets active=true).
     *
     * @param id project identifier
     * @return standardized response with unarchived project DTO
     */
    @Operation(
            summary = "Unarchive project",
            description = "Reactivates an archived project by setting active=true."
    )
    @PostMapping("/projects/{id}/unarchive")
    public TmsApiResponse<TmsProjectDto> unarchive(@PathVariable int id) {

        log.debug("POST /projects/{}/unarchive called", id);

        // Unarchive via service (set active=true)
        var unarchived = service.archiveProject(id, true);

        log.info("Project unarchived: id={}", id);

        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UNARCHIVED, unarchived);
    }

    /**
     * Deletes a project permanently by ID.
     *
     * @param id project identifier
     * @return standardized success response (no payload)
     */
    @Operation(
            summary = "Delete project",
            description = "Deletes a project permanently by its identifier."
    )
    @DeleteMapping("/projects/{id}")
    public TmsApiResponse<Void> delete(@PathVariable int id) {

        log.debug("DELETE /projects/{} requested", id);

        // Perform deletion in service layer
        service.deleteProject(id);

        log.info("Project deleted: id={}", id);

        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_DELETED, null);
    }

}
