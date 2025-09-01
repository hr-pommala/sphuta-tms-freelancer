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
 * Main API controller covering Clients (dropdown) and Projects CRUD.
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>All endpoints are versioned under <code>/api/v1</code>.</li>
 *   <li>Response payloads are wrapped with {@link TmsApiResponse} for consistent envelopes.</li>
 *   <li>Added logs at method entry/exit or just after service calls to aid traceability.</li>
 *   <li>No functional changes have been made—only comments and logging.</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@Validated
public class TmsProjectController {

    @Autowired
    private TmsProjectService service;

    /* ---------- Clients (Owner dropdown) ---------- */

    /**
     * Returns a paginated list of active clients for the Owner dropdown.
     *
     * @param active filter flag (true to fetch only active clients)
     * @param search optional case-insensitive name search
     * @param page   0-based page index
     * @param size   page size
     * @return standardized response envelope with page content + metadata
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
        var p = service.listClients(search, page, size);
        var meta = new TmsPageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        var resp = TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.CLIENTS_FETCHED, new TmsPageResponse<>(p.getContent(), meta));
        log.info("Clients list returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /* ----------------- Projects ------------------- */

    /**
     * Lists projects with filters (active, archived, by client, search).
     *
     * @param active   filter flag (true=active, false=archived)
     * @param clientId optional filter by owner (client id)
     * @param search   optional case-insensitive search on project name/code/description
     * @param page     0-based page index
     * @param size     page size
     * @return standardized response envelope with page content + metadata
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

        log.debug("GET /projects called: active={}, clientId={}, search='{}', page={}, size={}",
                active, clientId, search, page, size);

        var p = service.listProjects(active, clientId, search, page, size);

        var meta = new TmsPageResponse.PageMeta(
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );

        var resp = TmsApiResponse.success(
                HttpStatus.OK,
                ApiMessageConstants.PROJECTS_FETCHED_SUCCESS,
                new TmsPageResponse<>(p.getContent(), meta)
        );

        log.info("Projects list returned: elements={}, totalPages={}, page={}",
                p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /**
     * Creates a new project.
     *
     * @param in validated create payload
     * @return 201 Created with created resource in body and Location header set
     */
    @Operation(
            summary = "Create project",
            description = "Creates a new project under a client and returns the created resource with location header."
    )
    @PostMapping("/projects")
    public ResponseEntity<TmsApiResponse<TmsProjectDto>> create(
            @Validated(TmsProjectDto.Create.class) @RequestBody TmsProjectDto in) {

        log.debug("POST /projects create requested for name='{}', code='{}'", in.projectName(), in.code());
        var created = service.createProject(in);
        var location = URI.create("/api/v1/projects/" + created.id());
        var resp = TmsApiResponse.success(HttpStatus.CREATED, ApiMessageConstants.PROJECT_CREATED, created);
        log.info("Project created: id={}, location={}", created.id(), location);
        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Full update (PUT) of a project by ID.
     *
     * @param id project identifier
     * @param in validated update payload (full replacement)
     * @return updated project representation
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
        var updated = service.updateProject(id, in, true);
        log.info("Project fully updated: id={}", id);
        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UPDATED, updated);
    }

    /**
     * Partial update (PATCH) of a project by ID.
     *
     * @param id project identifier
     * @param in partial update payload (only provided fields are changed)
     * @return updated project representation
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
        var updated = service.updateProject(id, in, false);
        log.info("Project partially updated: id={}", id);
        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UPDATED, updated);
    }

    /**
     * Archives a project (sets active=false).
     *
     * @param id project identifier
     * @return updated project with archived status
     */
    @Operation(
            summary = "Archive project",
            description = "Archives a project by marking it as inactive."
    )
    @PostMapping("/projects/{id}/archive")
    public TmsApiResponse<TmsProjectDto> archive(@PathVariable int id) {

        log.debug("POST /projects/{}/archive called", id);
        var archived = service.archiveProject(id, false);
        log.info("Project archived: id={}", id);
        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_ARCHIVED, archived);
    }

    /**
     * Unarchives a project (sets active=true).
     *
     * @param id project identifier
     * @return updated project with active status
     */
    @Operation(
            summary = "Unarchive project",
            description = "Reactivates an archived project by setting active=true."
    )
    @PostMapping("/projects/{id}/unarchive")
    public TmsApiResponse<TmsProjectDto> unarchive(@PathVariable int id) {

        log.debug("POST /projects/{}/unarchive called", id);
        var unarchived = service.archiveProject(id, true);
        log.info("Project unarchived: id={}", id);
        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_UNARCHIVED, unarchived);
    }

    /**
     * Deletes a project by ID.
     *
     * @param id project identifier
     * @return success message
     */
    @Operation(
            summary = "Delete project",
            description = "Deletes a project permanently by its identifier."
    )
    @DeleteMapping("/projects/{id}")
    public TmsApiResponse<Void> delete(@PathVariable int id) {
        log.debug("DELETE /projects/{} requested", id);
        service.deleteProject(id);
        log.info("Project deleted: id={}", id);
        return TmsApiResponse.success(HttpStatus.OK, ApiMessageConstants.PROJECT_DELETED, null);
    }

}
