package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
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
 * ==========================================================
 * {@code TmsClientController}
 * ==========================================================
 *
 * REST controller for **Client Management**.
 *
 * <p><b>Responsibilities:</b></p>
 * - Handle CRUD operations for clients. <br>
 * - Archive/Unarchive client records. <br>
 * - List and export clients. <br>
 * - Delegate business logic to {@link TmsClientServiceImpl}. <br>
 * - Return consistent responses wrapped in {@link TmsApiResponse}. <br>
 *
 * <p><b>Design:</b></p>
 * - **Thin Controller** → No business logic, only request/response handling. <br>
 * - **Rich Documentation** → Annotated with Swagger for API documentation. <br>
 * - **Structured Logging** → Uses SLF4J for observability and debugging. <br>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(TmsMessages.CLIENT_BASE_PATH)
@Tag(name = "Clients", description = "Manage clients (list/search/export/CRUD)")
public class TmsClientController {

    @Autowired
    private TmsClientServiceImpl service;

    // ------------------------------------------------------------------------
    // LIST CLIENTS
    // ------------------------------------------------------------------------

    /**
     * Fetch paginated list of clients.
     *
     * @param active filter by active status ("true", "false", "all")
     * @param search search keyword
     * @param page   page number
     * @param size   page size
     * @return list of clients wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "List clients", description = "Fetch paginated list of clients with optional filtering by active status and search keyword")
    @GetMapping
    public ResponseEntity<TmsApiResponse<List<TmsClientDto>>> getClientlist(
            @RequestParam(defaultValue = "true") String active,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        log.debug("Fetching clients | active={}, search={}, page={}, size={}", active, search, page, size);

        Page<TmsClientDto> result = "all".equalsIgnoreCase(active)
                ? service.listAll(search, page, size)
                : service.getClientlist(Boolean.parseBoolean(active), search, page, size);

        log.info("Fetched {} clients", result.getNumberOfElements());

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENTS_FETCHED, result.getContent())
        );
    }

    // ------------------------------------------------------------------------
    // GET CLIENT
    // ------------------------------------------------------------------------

    /**
     * Get a single client by ID.
     *
     * @param id client identifier
     * @return client details wrapped in {@link TmsApiResponse}
     */
    @Operation(summary = "Get a client by ID", description = "Fetch a single client by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> get(@PathVariable int id) {
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
     * Create a new client.
     *
     * @param req  client request payload
     * @param http request object to build location header
     * @return created client with HTTP 201 status
     */
    @Operation(summary = "Create a client")
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
     * Replace a client record completely.
     *
     * @param id  client ID
     * @param req updated client payload
     * @return updated client
     */
    @Operation(summary = "Replace a client (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> replace(
            @PathVariable int id, @Valid @RequestBody TmsClientDto req) {

        log.debug("Replacing client id={} with payload={}", id, req);
        TmsClientDto resp = service.update(id, req);
        log.info("Client replaced successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_REPLACED, resp)
        );
    }


    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    /**
     * Delete client by ID.
     *
     * @param id client identifier
     * @return void response
     */
    @Operation(summary = "Delete a client")
    @DeleteMapping("/{id}")
    public ResponseEntity<TmsApiResponse<Void>> delete(@PathVariable int id) {
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
     * Archive client record (soft delete).
     *
     * @param id client identifier
     * @return archived client
     */
    @Operation(summary = "Archive a client")
    @PostMapping("/{id}/archive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> archive(@PathVariable int id) {
        log.debug("Archiving client id={}", id);
        TmsClientDto resp = service.archive(id);
        log.info("Client archived successfully | id={}", id);

        return ResponseEntity.ok(
                TmsApiResponse.success(HttpStatus.OK, TmsMessages.MSG_CLIENT_ARCHIVED, resp)
        );
    }

    /**
     * Unarchive client record.
     *
     * @param id client identifier
     * @return unarchived client
     */
    @Operation(summary = "Unarchive a client")
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<TmsApiResponse<TmsClientDto>> unarchive(@PathVariable int id) {
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
     * Export clients to CSV file.
     *
     * @param active filter by active status ("true", "false", "all")
     * @param search search keyword
     * @return CSV file as byte stream
     */
    @Operation(summary = "Export clients as CSV")
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(defaultValue = "all") String active,
            @RequestParam(defaultValue = "") String search) {

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