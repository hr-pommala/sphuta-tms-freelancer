package net.sphuta.tms.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sphuta.tms.dto.*;
import net.sphuta.tms.response.ApiResponse;
import net.sphuta.tms.response.PageResponse;
import net.sphuta.tms.service.TmsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

// Logging (kept lightweight; no behavior changes)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main API controller covering Clients (dropdown) and Projects CRUD.
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>All endpoints are versioned under <code>/api/v1</code>.</li>
 *   <li>Response payloads are wrapped with {@link ApiResponse} for consistent envelopes.</li>
 *   <li>Added logs at method entry/exit or just after service calls to aid traceability.</li>
 *   <li>No functional changes have been made—only comments and logging.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class TmsController {

    /** Class-scoped logger for endpoint tracing. */
    private static final Logger log = LoggerFactory.getLogger(TmsController.class);

    private final TmsService service;

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
    @Operation(summary = "Clients list for Owner dropdown")
    @GetMapping("/clients")
    public ApiResponse<PageResponse<ClientDto>> listClients(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        // --- Log inputs
        log.debug("GET /clients called: active={}, search='{}', page={}, size={}", active, search, page, size);

        // --- Delegate to service
        var p = service.listClients(search, page, size);

        // --- Build page metadata and wrap in envelope
        var meta = new PageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        var resp = ApiResponse.ok(new PageResponse<>(p.getContent(), meta));

        // --- Log summary
        log.info("Clients list returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /* ----------------- Projects ------------------- */

    /**
     * Active tab (default): lists projects where {@code active=true}.
     *
     * @param page 0-based page index
     * @param size page size
     * @return paginated active projects
     */
    @Operation(summary = "Projects — Active tab (default)")
    @GetMapping(value = "/projects", params = "active=true")
    public ApiResponse<PageResponse<ProjectDtos.View>> listActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        // --- Log inputs
        log.debug("GET /projects?active=true called: page={}, size={}", page, size);

        // --- Service call
        var p = service.listActiveProjects(page, size);

        // --- Build response
        var meta = new PageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        var resp = ApiResponse.ok(new PageResponse<>(p.getContent(), meta));

        // --- Log summary
        log.info("Active projects returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /**
     * Archived tab: lists projects where {@code active=false}.
     *
     * @param page 0-based page index
     * @param size page size
     * @return paginated archived projects
     */
    @Operation(summary = "Projects — Archived tab")
    @GetMapping(value = "/projects", params = "active=false")
    public ApiResponse<PageResponse<ProjectDtos.View>> listArchived(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        // --- Log inputs
        log.debug("GET /projects?active=false called: page={}, size={}", page, size);

        // --- Service call
        var p = service.listArchivedProjects(page, size);

        // --- Build response
        var meta = new PageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        var resp = ApiResponse.ok(new PageResponse<>(p.getContent(), meta));

        // --- Log summary
        log.info("Archived projects returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /**
     * Active tab filtered by owner + search.
     * <p>Matches exactly:
     * <pre>GET /projects?active=true&amp;clientId=&lt;CLIENT_ID&gt;&amp;search=backend&amp;page=0&amp;size=25</pre>
     *
     * @param clientId owner (client) identifier
     * @param search   case-insensitive search token
     * @param page     0-based page index
     * @param size     page size
     * @return paginated projects filtered by owner and search term (active only)
     */
    @Operation(summary = "Projects — Active tab filtered by Owner + Search")
    @GetMapping(value = "/projects", params = {"active=true", "clientId", "search"})
    public ApiResponse<PageResponse<ProjectDtos.View>> listActiveByOwnerAndSearch(
            @RequestParam String clientId,
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        // --- Log inputs
        log.debug("GET /projects?active=true&clientId={}&search='{}' called: page={}, size={}", clientId, search, page, size);

        // --- Service call
        var p = service.listActiveProjectsByOwnerAndSearch(clientId, search, page, size);

        // --- Build response
        var meta = new PageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        var resp = ApiResponse.ok(new PageResponse<>(p.getContent(), meta));

        // --- Log summary
        log.info("Active projects (owner+search) returned: elements={}, totalPages={}, page={}", p.getTotalElements(), p.getTotalPages(), p.getNumber());

        return resp;
    }

    /**
     * Creates a new project.
     *
     * @param in validated create payload
     * @return 201 Created with created resource in body and Location header set
     */
    @Operation(summary = "Create project")
    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectDtos.View>> create(@Valid @RequestBody ProjectDtos.Create in) {

        // --- Log intent (avoid logging full body if sensitive; here it's fine)
        log.debug("POST /projects create requested for name='{}', code='{}'", in.getProjectName(), in.getCode());

        // --- Service call
        var created = service.createProject(in);

        // --- Build Location and response
        var location = URI.create("/api/v1/projects/" + created.getId());
        var resp = ApiResponse.ok(created);

        // --- Log confirmation
        log.info("Project created: id={}, location={}", created.getId(), location);

        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Full update (PUT) of a project by ID.
     *
     * @param id project identifier
     * @param in validated update payload (full replacement)
     * @return updated project representation
     */
    @Operation(summary = "Full update (PUT)")
    @PutMapping("/projects/{id}")
    public ApiResponse<ProjectDtos.View> put(@PathVariable UUID id, @Valid @RequestBody ProjectDtos.Update in) {

        // --- Log inputs
        log.debug("PUT /projects/{} full update requested", id);

        // --- Service call (fullReplace=true)
        var updated = service.updateProject(id, in, true);

        // --- Log completion
        log.info("Project fully updated: id={}", id);

        return ApiResponse.ok(updated);
    }

    /**
     * Partial update (PATCH) of a project by ID.
     *
     * @param id project identifier
     * @param in partial update payload (only provided fields are changed)
     * @return updated project representation
     */
    @Operation(summary = "Partial update (PATCH)")
    @PatchMapping("/projects/{id}")
    public ApiResponse<ProjectDtos.View> patch(@PathVariable UUID id, @RequestBody ProjectDtos.Update in) {

        // --- Log inputs
        log.debug("PATCH /projects/{} partial update requested", id);

        // --- Service call (fullReplace=false)
        var updated = service.updateProject(id, in, false);

        // --- Log completion
        log.info("Project partially updated: id={}", id);

        return ApiResponse.ok(updated);
    }

    /**
     * Archives a project (sets active=false).
     *
     * @param id project identifier
     * @return updated project with archived status
     */
    @Operation(summary = "Archive project")
    @PostMapping("/projects/{id}/archive")
    public ApiResponse<ProjectDtos.View> archive(@PathVariable UUID id) {

        // --- Log intent
        log.debug("POST /projects/{}/archive called", id);

        // --- Service call
        var archived = service.archiveProject(id, false);

        // --- Log completion
        log.info("Project archived: id={}", id);

        return ApiResponse.ok(archived);
    }

    /**
     * Unarchives a project (sets active=true).
     *
     * @param id project identifier
     * @return updated project with active status
     */
    @Operation(summary = "Unarchive project")
    @PostMapping("/projects/{id}/unarchive")
    public ApiResponse<ProjectDtos.View> unarchive(@PathVariable UUID id) {

        // --- Log intent
        log.debug("POST /projects/{}/unarchive called", id);

        // --- Service call
        var unarchived = service.archiveProject(id, true);

        // --- Log completion
        log.info("Project unarchived: id={}", id);

        return ApiResponse.ok(unarchived);
    }

    /**
     * Deletes a project by ID.
     *
     * @param id project identifier
     * @return 204 No Content on successful deletion
     */
    @Operation(summary = "Delete project (hard delete)")
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        // --- Log intent
        log.warn("DELETE /projects/{} requested", id);

        // --- Service call
        service.deleteProject(id);

        // --- Log completion
        log.info("Project deleted: id={}", id);

        return ResponseEntity.noContent().build();
    }
}
