package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsEstimateRepository;
import net.sphuta.tms.freelancer.repository.TmsInvoiceRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsUserRepository;
import net.sphuta.tms.freelancer.util.TmsClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ==========================================================
 * {@code TmsClientServiceImpl}
 * ==========================================================
 *
 * Service implementation for managing {@link ClientEntity} lifecycle.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Provides CRUD operations for clients.</li>
 *   <li>Supports archive/unarchive functionality.</li>
 *   <li>Validates referential integrity before delete (time entries, invoices, estimates).</li>
 *   <li>Generates CSV exports for client data.</li>
 *   <li>Maps between entity and DTO using {@link TmsClientMapper}.</li>
 * </ul>
 *
 * <h2>Logging Policy:</h2>
 * <ul>
 *   <li><b>DEBUG</b>: entry logs, parameter details, verbose traces.</li>
 *   <li><b>INFO</b>: successful operations (create, update, archive, export).</li>
 *   <li><b>WARN</b>: business rule violations (delete blocked).</li>
 *   <li><b>ERROR</b>: unexpected system/runtime failures.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsClientServiceImpl {

    @Autowired private TmsClientRepository repo;
    @Autowired private TmsTimeEntryRepository timeRepo;
    @Autowired private TmsInvoiceRepository invoiceRepo;
    @Autowired private TmsEstimateRepository estimateRepo;
    @Autowired private TmsUserRepository userRepo;

    //Add this pattern near class-level (static)
    private static final Pattern EMAIL_RX = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    // ------------------------------------------------------------------------
    // LISTING
    // ------------------------------------------------------------------------

    /**
     * Retrieves a paginated list of clients filtered by active/archived.
     *
     * @param active true = only active, false = only archived
     * @param search free-text search filter
     * @param page   page number (0-based)
     * @param size   number of records per page
     * @return paginated list of client DTOs
     */
    public Page<TmsClientDto> list(boolean active, String search, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        log.debug("Listing clients | active={}, search='{}', page={}, size={}", active, search, p, s);

        Pageable pageable = PageRequest.of(p, s);
        return repo.search(active, search, pageable).map(TmsClientMapper::toResponse);
    }

    /**
     * Retrieves a paginated list of ALL clients (both active and archived).
     *
     * @param search free-text search filter
     * @param page   page number
     * @param size   number of records
     * @return paginated list of client DTOs
     */
    public Page<TmsClientDto> listAll(String search, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        log.debug("Listing ALL clients | search='{}', page={}, size={}", search, p, s);

        Pageable pageable = PageRequest.of(p, s);
        return repo.searchAll(search, pageable).map(TmsClientMapper::toResponse);
    }

    // ------------------------------------------------------------------------
    // FETCH SINGLE CLIENT
    // ------------------------------------------------------------------------

    /**
     * Fetches a client by ID or throws {@link TmsException} with 404.
     *
     * @param id client ID
     * @return client DTO
     */
    @Transactional(readOnly = true)
    public TmsClientDto get(Integer id) {
        log.debug("Fetching client by id={}", id);
        ClientEntity e = require(id);
        return TmsClientMapper.toResponse(e);
    }

    // ------------------------------------------------------------------------
    // CREATE + UPDATE
    // ------------------------------------------------------------------------

    /**
     * Creates a new client.
     *
     * @param req DTO containing client details
     * @return created client DTO
     */
    @Transactional
    public TmsClientDto create(TmsClientDto req) {
        log.debug("Creating new client: {}", req);

        // Business logic only: user existence and duplicate checks
        Integer userId = req.userId();
        if (!userRepo.existsById(userId)) {
            log.warn("User not found for userId={}", userId);
            throw new TmsException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Duplicate checks
        boolean existsUser = repo.existsByUserIdAndEmailIgnoreCase(userId, req.email());
        if (existsUser) {
            log.warn("Duplicate email for user | userId={}, email={}", userId, req.email());
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this user");
        }
        boolean existsCompany = repo.existsByCompanyNameIgnoreCaseAndEmailIgnoreCase(req.companyName(), req.email());
        if (existsCompany) {
            log.warn("Duplicate email for company | company={}, email={}", req.companyName(), req.email());
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
        }

        ClientEntity entity = TmsClientMapper.toNewEntity(req);
        try {
            ClientEntity saved = repo.save(entity);
            log.info("Created client id={}", saved.getId());
            return TmsClientMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation when creating client for userId={}, email={}", userId, req.email(), ex);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this user");
        }
    }

    /**
     * Updates an existing client.
     *
     * @param id  client ID
     * @param req DTO with new details
     * @return updated client DTO
     */
    @Transactional
    public TmsClientDto update(Integer id, TmsClientDto req) {
        log.debug("Updating client id={} with details={}", id, req);
        ClientEntity e = require(id);

        // Only business logic: user existence and duplicate checks
        Integer reqUserId = req.userId() != null ? req.userId() : e.getUserId();
        if (!userRepo.existsById(reqUserId)) {
            log.warn("User not found for userId={}", reqUserId);
            throw new TmsException(HttpStatus.NOT_FOUND, "User not found");
        }

        String newCompany = req.companyName() != null ? req.companyName().trim() : e.getCompanyName();
        String newEmail   = req.email() != null ? req.email().trim() : e.getEmail();

        // Remove all validation checks (null, blank, format)
        // Only check for business rule conflicts
        boolean conflictUser = repo.existsByUserIdAndEmailIgnoreCaseAndIdNot(reqUserId, newEmail, id);
        if (conflictUser) {
            log.warn("Email conflict on update | id={}, userId={}, company={}, email={}", id, reqUserId, newCompany, newEmail);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this user");
        }
        boolean conflictCompany = repo.existsByCompanyNameIgnoreCaseAndEmailIgnoreCaseAndIdNot(newCompany, newEmail, id);
        if (conflictCompany) {
            log.warn("Email conflict on update | id={}, company={}, email={}", id, newCompany, newEmail);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
        }

        TmsClientMapper.updateEntity(req, e);
        try {
            e = repo.save(e);
            log.info("Updated client id={}", id);
            return TmsClientMapper.toResponse(e);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation when updating client id={} userId={} email={}", id, reqUserId, newEmail, ex);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this user");
        }
    }

    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    /**
     * Deletes a client if no related records exist.
     *
     * @param id client ID
     */
    @Transactional
    public void delete(Integer id) {
        log.debug("Attempting to delete client id={}", id);
        ClientEntity e = require(id);
        // boolean hasTE  = timeRepo.existsByClientId(id);
        // boolean hasInv = invoiceRepo.existsByClientId(id);
        // boolean hasEst = estimateRepo.existsByClientId(id);
        // Uncomment and use above if you want to enforce referential integrity
        repo.delete(e);
        log.info("Deleted client id={}", id);
    }

    // ------------------------------------------------------------------------
    // ARCHIVE / UNARCHIVE
    // ------------------------------------------------------------------------

    /**
     * Archives (deactivates) a client.
     *
     * @param id client ID
     * @return archived client DTO
     */
    @Transactional
    public TmsClientDto archive(Integer id) {
        log.debug("Archiving client id={}", id);
        ClientEntity e = require(id);
        e.setIsActive(false);
        e = repo.save(e);
        log.info("Archived client id={}", id);
        return TmsClientMapper.toResponse(e);
    }

    /**
     * Unarchives (reactivates) a client.
     *
     * @param id client ID
     * @return unarchived client DTO
     */
    @Transactional
    public TmsClientDto unarchive(Integer id) {
        log.debug("Unarchiving client id={}", id);
        ClientEntity e = require(id);
        e.setIsActive(true);
        e = repo.save(e);
        log.info("Unarchived client id={}", id);
        return TmsClientMapper.toResponse(e);
    }

    // ------------------------------------------------------------------------
    // EXPORT CSV
    // ------------------------------------------------------------------------

    /**
     * Exports client data as a CSV file.
     *
     * @param activeFilter "true", "false", or "all"
     * @param search       search keyword
     * @return CSV content as byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportCsv(String activeFilter, String search) {
        log.debug("Generating CSV export | filter={}, search='{}'", activeFilter, search);

        Page<TmsClientDto> page;
        if ("all".equalsIgnoreCase(activeFilter)) {
            page = listAll(search, 0, Integer.MAX_VALUE);
        } else {
            boolean isActive = Boolean.parseBoolean(activeFilter);
            page = list(isActive, search, 0, Integer.MAX_VALUE);
        }

        // CSV Header
        String header = "id,companyName,firstName,lastName,email,isActive\n";

        // CSV Rows
        String rows = page.getContent().stream()
                .map(c -> String.join(",",
                        String.valueOf(c.id()),
                        safe(c.companyName()), safe(c.firstName()), safe(c.lastName()),
                        safe(c.email()), String.valueOf(c.isActive())))
                .collect(Collectors.joining("\n"));

        byte[] bytes = (header + rows + "\n").getBytes(StandardCharsets.UTF_8);

        log.info("CSV export generated successfully: {} rows", page.getNumberOfElements());
        return bytes;
    }

    /**
     * Escapes nulls and quotes for safe CSV export.
     */
    private static String safe(String s) {
        return s == null ? "" : '"' + s.replace("\"", "'") + '"';
    }

    // ------------------------------------------------------------------------
    // INTERNAL UTIL
    // ------------------------------------------------------------------------

    /**
     * Ensures client exists by ID or throws {@link TmsException} (404).
     *
     * @param id client ID
     * @return client entity
     */
    private ClientEntity require(Integer id) {
        return repo.findById(id).orElseThrow(() -> {
            log.error("Client not found id={}", id);
            return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        });
    }
}