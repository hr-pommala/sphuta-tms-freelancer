package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.TmsEntity;
import net.sphuta.tms.freelancer.service.impl.TmsServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <h2>Clients Controller</h2>
 * Implements list/search/export + CRUD + archive/unarchive.
 *
 * <p><b>Logging policy</b>:
 * <ul>
 *   <li><b>DEBUG</b>: method entry + key parameters (active/search/page/size/ids)</li>
 *   <li><b>INFO</b>: successful high-level outcomes (counts, creates, exports)</li>
 *   <li><b>WARN</b>: suspicious but non-fatal inputs (e.g., page &lt; 0, size &gt; 100)</li>
 *   <li><b>TRACE</b>: fine-grained info (e.g., CSV bytes length)</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Manage clients (list/search/export/CRUD)")
public class TmsController {

    /** Business service handling client operations. */
    private final TmsServiceImpl service;

    /**
     * List clients with Active/Archived filter, free-text search, and pagination.
     *
     * @param active true for Active tab, false for Archived
     * @param search case-insensitive search across company/name/email
     * @param page   zero-based page index
     * @param size   page size (recommended max 100)
     * @return API response wrapper containing page slice + meta
     */
    @Operation(summary = "List clients (Active/Archived + Search)")
    @GetMapping
    public ResponseEntity<TmsDto.ApiResponse<List<TmsDto.ClientResponse>>> list(
            @Parameter(description = "true=Active tab, false=Archived tab", example = "true")
            @RequestParam(defaultValue = "true") boolean active,
            @Parameter(description = "Search text across company/name/email", example = "acme")
            @RequestParam(defaultValue = "") String search,
            @Parameter(description = "Page (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "25") int size) {

        // ── entry logs (non-invasive) ───────────────────────────────────────────────
        log.debug("HTTP GET /clients active={}, search='{}', page={}, size={}", active, search, page, size);
        if (page < 0) {
            log.warn("List clients called with negative page index: {}", page);
        }
        if (size > 100) {
            log.warn("List clients requested with large page size: {} (recommended <= 100)", size);
        }

        Page<TmsEntity> result = service.list(active, search, page, size);
        List<TmsDto.ClientResponse> data = result.getContent().stream().map(this::toDto).toList();

        var body = TmsDto.ApiResponse.<List<TmsDto.ClientResponse>>builder()
                .message("Clients fetched")
                .data(data)
                .meta(TmsDto.ApiResponse.Meta.builder()
                        .total(result.getTotalElements()).page(result.getNumber()).size(result.getSize()).build())
                .build();

        log.info("Returned {} clients (page {}/{})", data.size(), result.getNumber(), result.getTotalPages());
        return ResponseEntity.ok(body);
    }

    /**
     * Fetch a single client by its identifier.
     *
     * @param id client UUID
     * @return 200 with client, or 404 if not found
     */
    @Operation(summary = "Get client by id")
    @GetMapping("/{id}")
    public ResponseEntity<TmsDto.ClientResponse> get(@PathVariable("id") UUID id) {
        log.debug("HTTP GET /clients/{} (fetch by id)", id);
        return service.find(id).map(e -> {
            log.debug("Client {} found", id);
            return ResponseEntity.ok(toDto(e));
        }).orElseGet(() -> {
            log.warn("Client {} not found", id);
            return ResponseEntity.notFound().build();
        });
    }

    /**
     * Create a new client.
     *
     * @param req  validated create payload (DTO record)
     * @param http request for building Location header
     * @return 201 Created with Location header and client payload
     */
    @Operation(summary = "Create client")
    @PostMapping
    public ResponseEntity<TmsDto.ClientResponse> create(@Valid @RequestBody TmsDto.ClientRequest req,
                                                        HttpServletRequest http) {
        // basic request logging
        log.debug("HTTP POST /clients email={}, companyName={}", req.email(), req.companyName());
        TmsEntity saved = service.create(req);

        // success log
        log.info("Client {} created", saved.getId());

        return ResponseEntity.created(URI.create(http.getRequestURI() + "/" + saved.getId()))
                .body(toDto(saved));
    }

    /**
     * Replace a client (full update via PUT).
     *
     * @param id  client UUID
     * @param req validated request body
     * @return updated client payload
     */
    @Operation(summary = "Replace client (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<TmsDto.ClientResponse> replace(@PathVariable UUID id,
                                                         @Valid @RequestBody TmsDto.ClientRequest req) {
        log.debug("HTTP PUT /clients/{} (replace)", id);
        TmsDto.ClientResponse resp = toDto(service.update(id, req));
        log.info("Client {} replaced", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * Partially update a client via PATCH.
     *
     * @param id  client UUID
     * @param req partial update payload
     * @return updated client payload
     */
    @Operation(summary = "Patch client (partial update)")
    @PatchMapping("/{id}")
    public ResponseEntity<TmsDto.ClientResponse> patch(@PathVariable UUID id,
                                                       @RequestBody TmsDto.ClientRequest req) {
        log.debug("HTTP PATCH /clients/{} (partial update)", id);
        TmsDto.ClientResponse resp = toDto(service.update(id, req));
        log.info("Client {} patched", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * Delete a client. May return 409 if related records exist.
     *
     * @param id client UUID
     * @return 204 No Content on success
     */
    @Operation(summary = "Delete client")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.debug("HTTP DELETE /clients/{}", id);
        service.delete(id);
        log.info("Client {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Archive a client (sets isActive=false).
     *
     * @param id client UUID
     * @return updated client payload
     */
    @Operation(summary = "Archive client (move to Archived tab)")
    @PostMapping("/{id}/archive")
    public ResponseEntity<TmsDto.ClientResponse> archive(@PathVariable UUID id) {
        log.debug("HTTP POST /clients/{}/archive", id);
        TmsDto.ClientResponse resp = toDto(service.archive(id));
        log.info("Client {} archived", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * Unarchive a client (sets isActive=true).
     *
     * @param id client UUID
     * @return updated client payload
     */
    @Operation(summary = "Unarchive client (restore Active)")
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<TmsDto.ClientResponse> unarchive(@PathVariable UUID id) {
        log.debug("HTTP POST /clients/{}/unarchive", id);
        TmsDto.ClientResponse resp = toDto(service.unarchive(id));
        log.info("Client {} unarchived", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * Export clients to CSV. Honors "active" & "search" parameters.
     * Produces <code>text/csv</code> with <code>Content-Disposition: attachment</code>.
     *
     * @param active "all", "true" or "false"
     * @param search search text
     * @return CSV file as byte array
     */
    @Operation(summary = "Export clients as CSV", description = "Produces text/csv")
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(defaultValue = "all") String active,
                                            @RequestParam(defaultValue = "") String search) {
        // entry log
        log.debug("HTTP GET /clients/export active='{}', search='{}'", active, search);

        boolean actFilter = !active.equalsIgnoreCase("all");
        Page<TmsEntity> page = service.list(actFilter ? Boolean.parseBoolean(active) : true, search, 0, Integer.MAX_VALUE);

        // CSV header
        String header = "id,companyName,firstName,lastName,email,isActive\n";

        // CSV rows (escaping double-quotes by replacing them with single quotes for simplicity)
        String rows = page.getContent().stream().map(c -> String.join(",",
                        c.getId().toString(),
                        safe(c.getCompanyName()), safe(c.getFirstName()), safe(c.getLastName()),
                        safe(c.getEmail()), String.valueOf(c.isActive())))
                .collect(Collectors.joining("\n"));

        byte[] bytes = (header + rows + "\n").getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("clients.csv").build());

        log.info("CSV export generated: {} rows", page.getNumberOfElements());
        log.trace("CSV export payload size (bytes): {}", bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    // ---- helpers ----

    /**
     * Map entity to read-model DTO record.
     * This keeps controller responses stable and avoids leaking entity internals.
     */
    private TmsDto.ClientResponse toDto(TmsEntity e) {
        // trace-level log helps debug mapping without spamming normal logs
        log.trace("Mapping TmsEntity {} to ClientResponse DTO", e.getId());
        return TmsDto.ClientResponse.builder()
                .id(e.getId()).companyName(e.getCompanyName()).firstName(e.getFirstName()).lastName(e.getLastName())
                .email(e.getEmail()).mobilePhone(e.getMobilePhone()).businessPhone(e.getBusinessPhone())
                .addressLine1(e.getAddressLine1()).addressLine2(e.getAddressLine2())
                .city(e.getCity()).state(e.getState()).postalCode(e.getPostalCode()).countryCode(e.getCountryCode())
                .sendReminders(e.isSendReminders()).chargeLateFees(e.isChargeLateFees()).lateFeePercent(e.getLateFeePercent())
                .currencyCode(e.getCurrencyCode()).language(e.getLanguage()).allowInvoiceAttachments(e.isAllowInvoiceAttachments())
                .isActive(e.isActive()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    /**
     * Very simple CSV cell “escaper”: wraps value in quotes and replaces any inner double quotes with single quotes.
     * (Sufficient for demo CSV generation; use a dedicated CSV library for complex data.)
     */
    private static String safe(String s) { return s == null ? "" : '"' + s.replace("\"","'") + '"'; }
}
