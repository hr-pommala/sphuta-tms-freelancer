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
import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * TmsClientController
 * ============================================================
 * REST controller for managing Client entities.
 * <p>
 * Features:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Archive/Unarchive functionality (soft delete)
 * - Paginated listing with search and filtering
 * - Export functionality (CSV download)
 * <p>
 * This controller delegates business logic to {@link TmsClientServiceImpl}.
 * Responses are wrapped inside {@link TmsApiResponse} for consistency.
 * ============================================================
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(TmsMessages.CLIENT_BASE_PATH)
@Tag(name = "Clients", description = "Manage clients (list/search/export/CRUD)")
public class TmsClientController {

    /** Service layer dependency for client operations */
    @Autowired
    private TmsClientServiceImpl service;

    // ------------------------------------------------------------------------
    // LIST CLIENTS
    // ------------------------------------------------------------------------

    /**
     * Fetches a paginated list of clients.
     *
     * @param active Filter by active status: true, false, or all
     * @param search Keyword for searching by client name or attributes
     * @param page   Page number (0-based index)
     * @param size   Number of records per page
     * @return Paginated list of clients wrapped in {@link TmsApiResponse}
     */
    @Operation(
            summary = "List clients",
            description = "Fetch paginated list of clients. Supports filtering by active status and keyword search."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients fetched successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TmsClientDto.class)))
    })
    @GetMapping
    public ResponseEntity<TmsApiResponse<List<TmsClientDto>>> list(
            @Parameter(description = "Filter by active status: true, false, or all") @RequestParam(defaultValue = "true") String active,
            @Parameter(description = "Search keyword") @RequestParam(defaultValue = "") String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "25") int size) {

        log.debug("Fetching clients | active={}, search={}, page={}, size={}", active, search, page, size);

        Page<TmsClientDto> result = "all".equalsIgnoreCase(active)
                ? service.listAll(search, page, size)
                : service.list(Boolean.parseBoolean(active), search, page, size);

        log.info("Fetched {} clients", result.getNumberOfElements());

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENTS_FETCHED, result.getContent())
        );
    }

    // ------------------------------------------------------------------------
    // GET CLIENT
    // ------------------------------------------------------------------------

    /**
     * Retrieves a single client by ID.
     *
     * @param id Client identifier
     * @return Client details wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Get a client by ID", description = "Fetch a single client by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client fetched successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> get(@PathVariable Integer id) {
        log.debug("Fetching client with id={}", id);
        TmsClientDto dto = service.get(id);
        log.info("Client fetched successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_FETCHED, dto)
        );
    }

    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    /**
     * Creates a new client.
     *
     * @param req  Client details payload
     * @param http HTTP request (used to build resource location URI)
     * @return Newly created client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Create a client", description = "Create a new client record with the given details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    @PostMapping
    public ResponseEntity<TmsApiResponse<TmsClientDto>> create(
            @Valid @RequestBody TmsClientDto req, HttpServletRequest http) {

        log.debug("Creating client with payload: {}", req);
        TmsClientDto saved = service.create(req);
        URI location = URI.create(http.getRequestURI() + "/" + saved.id());

        log.info("Client created successfully | id={}", saved.id());

        return ResponseEntity.created(location)
                .body(TmsApiResponse.success(HttpStatus.CREATED, TmsMessages.MSG_CLIENT_CREATED, saved));
    }

    // ------------------------------------------------------------------------
    // UPDATE (PUT)
    // ------------------------------------------------------------------------

    /**
     * Replaces an existing client completely (PUT).
     *
     * @param id  Client identifier
     * @param req New client payload
     * @return Updated client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Replace a client (PUT)", description = "Replace an existing client record completely.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client replaced successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> replace(
            @PathVariable Integer id, @Valid @RequestBody TmsClientDto req) {

        log.debug("Replacing client id={} with payload={}", id, req);
        TmsClientDto resp = service.update(id, req);
        log.info("Client replaced successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_REPLACED, resp)
        );
    }

    // ------------------------------------------------------------------------
    // PATCH
    // ------------------------------------------------------------------------

    /**
     * Partially updates a client (PATCH).
     *
     * @param id  Client identifier
     * @param req Partial client payload (only fields to update)
     * @return Updated client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Patch a client (partial update)", description = "Update only specific fields of a client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client patched successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> patch(
            @PathVariable Integer id, @RequestBody TmsClientDto req) {

        log.debug("Patching client id={} with changes={}", id, req);
        TmsClientDto resp = service.update(id, req);
        log.info("Client patched successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_PATCHED, resp)
        );
    }

    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    /**
     * Deletes a client permanently.
     *
     * @param id Client identifier
     * @return Success message wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Delete a client", description = "Delete a client record permanently by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<TmsApiResponse<Void>> delete(@PathVariable Integer id) {
        log.debug("Deleting client with id={}", id);
        service.delete(id);
        log.info("Client deleted successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_DELETED, null)
        );
    }

    // ------------------------------------------------------------------------
    // ARCHIVE / UNARCHIVE
    // ------------------------------------------------------------------------

    /**
     * Archives a client (soft delete).
     *
     * @param id Client identifier
     * @return Archived client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Archive a client", description = "Soft delete a client (mark as archived).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client archived successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PostMapping("/{id}/archive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> archive(@PathVariable Integer id) {
        log.debug("Archiving client id={}", id);
        TmsClientDto resp = service.archive(id);
        log.info("Client archived successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_ARCHIVED, resp)
        );
    }

    /**
     * Restores an archived client back to active state.
     *
     * @param id Client identifier
     * @return Unarchived client wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Unarchive a client", description = "Restore an archived client back to active state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client unarchived successfully",
                    content = @Content(schema = @Schema(implementation = TmsClientDto.class))),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> unarchive(@PathVariable Integer id) {
        log.debug("Unarchiving client id={}", id);
        TmsClientDto resp = service.unarchive(id);
        log.info("Client unarchived successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_UNARCHIVED, resp)
        );
    }

    // ------------------------------------------------------------------------
    // EXPORT
    // ------------------------------------------------------------------------

    /**
     * Exports clients as CSV file.
     *
     * @param active Filter by active status (true, false, all)
     * @param search Keyword for searching clients
     * @return CSV file as byte array in response
     */
    @Operation(summary = "Export clients as CSV", description = "Export filtered clients into a downloadable CSV file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV exported successfully",
                    content = @Content(mediaType = "text/csv")),
    })
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @Parameter(description = "Filter by active status: true, false, all") @RequestParam(defaultValue = "all") String active,
            @Parameter(description = "Search keyword") @RequestParam(defaultValue = "") String search) {

        log.debug("Exporting clients to CSV | active={}, search={}", active, search);
        byte[] bytes = service.exportCsv(active, search);

        log.info("CSV export completed | size={} bytes", bytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("clients.csv").build());
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setLastModified(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
