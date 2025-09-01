package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.impl.TmsClientServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ==========================================================
 * {@code TmsClientController}
 * ==========================================================
 *
 * <p>
 * REST controller that manages **Clients** in the system:
 * listing, searching, exporting, and CRUD operations.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Provide endpoints for listing/searching clients with pagination.</li>
 *   <li>Provide CRUD operations (Create, Read, Update, Delete).</li>
 *   <li>Support archive/unarchive lifecycle operations.</li>
 *   <li>Support CSV export of client data.</li>
 * </ul>
 *
 * <h2>Design notes:</h2>
 * <ul>
 *   <li>Controller remains thin – business logic is delegated to {@link TmsClientServiceImpl}.</li>
 *   <li>Uses {@link TmsMessages} for consistent message/log templates.</li>
 *   <li>Provides rich Swagger/OpenAPI documentation with {@code @Operation} and {@code @ApiResponse} annotations.</li>
 *   <li>Structured logging with SLF4J for observability.</li>
 *   <li>Execution timing with {@code System.nanoTime()} for performance insight.</li>
 * </ul>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(TmsMessages.CLIENT_BASE_PATH)
@Tag(name = "Clients", description = "Manage clients (list/search/export/CRUD)")
public class TmsClientController {

    /** Service layer handling client domain operations. */
    @Autowired
    private TmsClientServiceImpl service;


    // ------------------------------------------------------------------------
    // LIST CLIENTS
    // ------------------------------------------------------------------------

    /**
     * List clients with optional filters and pagination.
     *
     * @param active filter by active (true) or archived (false)
     * @param search free-text search across company/name/email
     * @param page   0-based page index
     * @param size   number of records per page
     * @return paginated list of clients wrapped in {@link TmsApiResponse}
     */
    @Operation(
            summary = "List clients",
            description = "Returns a paginated list of clients. Filter by Active/Archived and search across company/name/email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = TmsMessages.MSG_CLIENTS_FETCHED,
                    content = @Content(schema = @Schema(implementation = TmsApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = TmsApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<TmsApiResponse<List<TmsClientDto>>> list(
            @Parameter(description = "true=Active tab, false=Archived tab", example = "true")
            @RequestParam(defaultValue = "true") boolean active,
            @Parameter(description = "Search text across company/name/email", example = "acme")
            @RequestParam(defaultValue = "") String search,
            @Parameter(description = "Page (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "25")
            @RequestParam(defaultValue = "25") int size) {

        log.debug(TmsMessages.LOG_CLIENT_LIST_REQUEST, active, search, page, size);
        long _startNs = System.nanoTime();

        Page<TmsClientDto> result = service.list(active, search, page, size);
        List<TmsClientDto> data = result.getContent();

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.info("Returned {} clients (page {}/{})", data.size(), result.getNumber(), result.getTotalPages());
        log.debug("List clients completed in {} ms (hasNext={}, totalElements={})",
                _tookMs, result.hasNext(), result.getTotalElements());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENTS_FETCHED, data));
    }

    // ------------------------------------------------------------------------
    // GET CLIENT
    // ------------------------------------------------------------------------

    /**
     * Get a single client by ID.
     *
     * @param id numeric ID of the client
     * @return single client or 404 if not found
     */
    @Operation(
            summary = "Get a client by ID",
            description = "Returns a single client by numeric auto-increment ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = TmsMessages.MSG_CLIENT_FETCHED,
                    content = @Content(schema = @Schema(implementation = TmsApiResponse.class))),
            @ApiResponse(responseCode = "404", description = TmsMessages.ERR_CLIENT_NOT_FOUND,
                    content = @Content(schema = @Schema(implementation = TmsApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> get(@PathVariable("id") Integer id) {

        log.debug(TmsMessages.LOG_CLIENT_GET_BY_ID, id);
        long _startNs = System.nanoTime();

        Page<TmsClientDto> page = service.list(true, "", 0, Integer.MAX_VALUE);
        Optional<TmsClientDto> match = page.getContent().stream()
                .filter(c -> c.id().equals(id))
                .findFirst();

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;

        return match
                .map(m -> ResponseEntity.ok(
                        TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_FETCHED, m)
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(TmsApiResponse.<TmsClientDto>error(
                                HttpStatus.NOT_FOUND,
                                TmsMessages.ERR_CLIENT_NOT_FOUND
                        )));


    }

    // ------------------------------------------------------------------------
    // CREATE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Create a new client.
     *
     * @param req  client creation payload
     * @param http request metadata (used for Location header)
     * @return created client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Create a client",
            description = "Creates a new client. The `id` is assigned automatically and returned in the response.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = TmsMessages.MSG_CLIENT_CREATED,
                    content = @Content(schema = @Schema(implementation = TmsApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<TmsApiResponse<TmsClientDto>> create(
            @Valid @RequestBody TmsClientDto req,
            HttpServletRequest http) {

        log.debug(TmsMessages.LOG_CLIENT_CREATE_REQUEST, req.email(), req.companyName());
        long _startNs = System.nanoTime();

        TmsClientDto saved = service.create(req);
        log.info(TmsMessages.MSG_CLIENT_CREATED + " id={}", saved.id());

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Create client completed in {} ms (newId={})", _tookMs, saved.id());

        URI location = URI.create(http.getRequestURI() + "/" + saved.id());
        return ResponseEntity.created(location)
                .body(TmsApiResponse.success(HttpStatus.CREATED, TmsMessages.MSG_CLIENT_CREATED, saved));
    }

    // ------------------------------------------------------------------------
    // REPLACE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Replace (full update) a client by ID.
     *
     * @param id  client ID
     * @param req new client payload
     * @return updated client
     */
    @Operation(summary = "Replace a client (PUT)",
            description = "Full update of a client by ID.")
    @PutMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> replace(
            @PathVariable Integer id,
            @Valid @RequestBody TmsClientDto req) {

        log.debug(TmsMessages.LOG_CLIENT_UPDATE_REQUEST, id);
        long _startNs = System.nanoTime();

        TmsClientDto resp = service.update(id, req);
        log.info(TmsMessages.MSG_CLIENT_REPLACED + " id={}", id);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Replace client {} completed in {} ms", id, _tookMs);

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_REPLACED, resp));
    }

    // ------------------------------------------------------------------------
    // PATCH CLIENT
    // ------------------------------------------------------------------------

    /**
     * Partially update a client by ID.
     *
     * @param id  client ID
     * @param req partial payload
     * @return updated client
     */
    @Operation(summary = "Patch a client (partial update)")
    @PatchMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> patch(
            @PathVariable Integer id,
            @RequestBody TmsClientDto req) {

        log.debug(TmsMessages.LOG_CLIENT_PATCH_REQUEST, id);
        long _startNs = System.nanoTime();

        TmsClientDto resp = service.update(id, req);
        log.info(TmsMessages.MSG_CLIENT_PATCHED + " id={}", id);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Patch client {} completed in {} ms", id, _tookMs);

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_PATCHED, resp));
    }

    // ------------------------------------------------------------------------
    // DELETE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Delete a client by ID.
     *
     * @param id client ID
     * @return void response
     */
    @Operation(summary = "Delete a client")
    @DeleteMapping("/{id}")
    public ResponseEntity<TmsApiResponse<Void>> delete(@PathVariable Integer id) {

        log.debug(TmsMessages.LOG_CLIENT_DELETE_REQUEST, id);
        long _startNs = System.nanoTime();

        service.delete(id);
        log.info(TmsMessages.MSG_CLIENT_DELETED + " id={}", id);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Delete client {} completed in {} ms", id, _tookMs);

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_DELETED, null));
    }

    // ------------------------------------------------------------------------
    // ARCHIVE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Archive a client (set inactive).
     *
     * @param id client ID
     * @return updated client
     */
    @Operation(summary = "Archive a client")
    @PostMapping("/{id}/archive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> archive(@PathVariable Integer id) {

        log.debug(TmsMessages.LOG_CLIENT_ARCHIVE_REQUEST, id);
        long _startNs = System.nanoTime();

        TmsClientDto resp = service.archive(id);
        log.info(TmsMessages.MSG_CLIENT_ARCHIVED + " id={}", id);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Archive client {} completed in {} ms (isActive={})", id, _tookMs, resp.isActive());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_ARCHIVED, resp));
    }

    // ------------------------------------------------------------------------
    // UNARCHIVE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Unarchive a client (restore to active).
     *
     * @param id client ID
     * @return updated client
     */
    @Operation(summary = "Unarchive a client")
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> unarchive(@PathVariable Integer id) {

        log.debug(TmsMessages.LOG_CLIENT_UNARCHIVE_REQUEST, id);
        long _startNs = System.nanoTime();

        TmsClientDto resp = service.unarchive(id);
        log.info(TmsMessages.MSG_CLIENT_UNARCHIVED + " id={}", id);

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.debug("Unarchive client {} completed in {} ms (isActive={})", id, _tookMs, resp.isActive());

        return ResponseEntity.ok(TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_UNARCHIVED, resp));
    }

    // ------------------------------------------------------------------------
    // EXPORT CLIENTS
    // ------------------------------------------------------------------------

    /**
     * Export clients as CSV.
     *
     * @param active "true", "false", or "all"
     * @param search full-text search
     * @return downloadable CSV file
     */
    @Operation(summary = "Export clients as CSV")
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @Parameter(description = "Filter by 'true', 'false', or 'all'", example = "all")
            @RequestParam(defaultValue = "all") String active,
            @Parameter(description = "Full-text search across company/name/email", example = "acme")
            @RequestParam(defaultValue = "") String search) {

        log.debug(TmsMessages.LOG_CLIENT_EXPORT_REQUEST, active, search);
        long _startNs = System.nanoTime();

        boolean actFilter = !active.equalsIgnoreCase("all");
        Page<TmsClientDto> page = service.list(
                actFilter ? Boolean.parseBoolean(active) : true,
                search, 0, Integer.MAX_VALUE);

        // Build CSV payload
        String header = "id,companyName,firstName,lastName,email,isActive\n";
        String rows = page.getContent().stream().map(c -> String.join(",",
                        String.valueOf(c.id()),
                        safe(c.companyName()), safe(c.firstName()), safe(c.lastName()),
                        safe(c.email()), String.valueOf(c.isActive())))
                .collect(Collectors.joining("\n"));

        byte[] bytes = (header + rows + "\n").getBytes(StandardCharsets.UTF_8);

        // HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("clients.csv").build());
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setLastModified(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());

        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.info("CSV export generated: {} rows", page.getNumberOfElements());
        log.debug("Export clients completed in {} ms (pageSize={}, totalElements={})",
                _tookMs, page.getSize(), page.getTotalElements());
        log.trace("CSV export payload size (bytes): {}", bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * Escapes CSV values safely by quoting and replacing embedded quotes.
     *
     * @param s raw string
     * @return safe CSV value
     */
    private static String safe(String s) {
        return s == null ? "" : '"' + s.replace("\"", "'") + '"';
    }
}
