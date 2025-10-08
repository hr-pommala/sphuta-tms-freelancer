package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.*;
import net.sphuta.tms.freelancer.service.TmsClientService;
import net.sphuta.tms.freelancer.util.TmsClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ==========================================================
 * {@code TmsClientServiceImpl}
 * ==========================================================
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Implements {@link TmsClientService} contract.</li>
 *     <li>Manages lifecycle of {@link ClientEntity} including CRUD, archive/unarchive, export.</li>
 *     <li>Handles duplicate checks, referential integrity, and error handling.</li>
 *     <li>Maps between {@link ClientEntity} and {@link TmsClientDto} using {@link TmsClientMapper}.</li>
 * </ul>
 *
 * <h2>Logging Policy:</h2>
 * <ul>
 *     <li>DEBUG → Entry logs, input parameters.</li>
 *     <li>INFO → Successful CRUD/archive/export operations.</li>
 *     <li>WARN → Conflict/constraint violations.</li>
 *     <li>ERROR → Entity not found or unexpected system failures.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsClientServiceImpl implements TmsClientService {

    /** Repository for {@link ClientEntity} CRUD operations */
    @Autowired
    private TmsClientRepository repo;

    /** Repository for time entry references */
    @Autowired
    private TmsTimeEntryRepository timeRepo;

    /** Repository for invoice references */
    @Autowired
    private TmsInvoiceRepository invoiceRepo;

    /** Repository for estimate references */
//    @Autowired
//    private TmsEstimateRepository estimateRepo;

    /** Repository for user validation */
    @Autowired
    private TmsUserRepository userRepo;

    // ------------------------------------------------------------------------
    // LISTING
    // ------------------------------------------------------------------------

    /**
     * Retrieve paginated list of clients filtered by active/archived flag.
     *
     * @param active true = only active, false = only archived
     * @param search free-text search filter
     * @param page   page number (0-based)
     * @param size   number of records per page
     * @return paginated {@link Page} of {@link TmsClientDto}
     */
    @Override
    public Page<TmsClientDto> getClientlist(boolean active, String search, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        log.debug("Listing clients | active={}, search='{}', page={}, size={}", active, search, p, s);

        Pageable pageable = PageRequest.of(p, s);
        return repo.search(active, search, pageable).map(TmsClientMapper::toResponse);
    }

    /**
     * Retrieve paginated list of all clients (active + archived).
     *
     * @param search free-text search filter
     * @param page   page number (0-based)
     * @param size   number of records per page
     * @return paginated {@link Page} of {@link TmsClientDto}
     */
    @Override
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
     * Get a client by ID.
     *
     * @param id client ID
     * @return {@link TmsClientDto}
     * @throws TmsException if client not found
     */
    @Override
    public TmsClientDto get(int id) {
        log.debug("Fetching client by id={}", id);
        return repo.findById(id)
                .map(TmsClientMapper::toResponse)
                .orElseThrow(() -> {
                    log.error("Client not found id={}", id);
                    return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
                });
    }

    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    /**
     * Create a new client record.
     *
     * @param req {@link TmsClientDto} request payload
     * @return created client DTO
     */
    @Override
    public TmsClientDto create(TmsClientDto req) {
        log.debug("Creating new client: {}", req);

        int userId = req.userId();
        if (!userRepo.existsById(userId)) {
            log.warn("User not found for userId={}", userId);
            throw new TmsException(HttpStatus.NOT_FOUND, "User not found");
        }

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

    // ------------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------------

    /**
     * Update an existing client.
     *
     * @param id  client ID
     * @param req {@link TmsClientDto} payload with new details
     * @return updated client DTO
     */
    @Override
    public TmsClientDto update(int id, TmsClientDto req) {
        log.debug("Updating client id={} with details={}", id, req);

        ClientEntity e = repo.findById(id).orElseThrow(() -> {
            log.error("Client not found id={}", id);
            return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        });

        int reqUserId = Optional.ofNullable(req.userId()).orElse(e.getUserId());
        if (!userRepo.existsById(reqUserId)) {
            log.warn("User not found for userId={}", reqUserId);
            throw new TmsException(HttpStatus.NOT_FOUND, "User not found");
        }

        String newCompany = Optional.ofNullable(req.companyName()).map(String::trim).orElse(e.getCompanyName());
        String newEmail   = Optional.ofNullable(req.email()).map(String::trim).orElse(e.getEmail());

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
     * Delete client by ID.
     *
     * @param id client ID
     */
    @Override
    public void delete(int id) {
        log.debug("Attempting to delete client id={}", id);
        ClientEntity e = repo.findById(id).orElseThrow(() -> {
            log.error("Client not found id={}", id);
            return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        });

        // Referential integrity checks can be re-enabled here if required
        repo.delete(e);
        log.info("Deleted client id={}", id);
    }

    // ------------------------------------------------------------------------
    // ARCHIVE / UNARCHIVE
    // ------------------------------------------------------------------------

    /**
     * Archive (soft deactivate) client by ID.
     *
     * @param id client ID
     * @return archived {@link TmsClientDto}
     */
    @Override
    public TmsClientDto archive(int id) {
        log.debug("Archiving client id={}", id);
        ClientEntity e = repo.findById(id).orElseThrow(() -> {
            log.error("Client not found id={}", id);
            return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        });
        e.setIsActive(false);
        e = repo.save(e);
        log.info("Archived client id={}", id);
        return TmsClientMapper.toResponse(e);
    }

    /**
     * Unarchive (reactivate) client by ID.
     *
     * @param id client ID
     * @return unarchived {@link TmsClientDto}
     */
    @Override
    public TmsClientDto unarchive(int id) {
        log.debug("Unarchiving client id={}", id);
        ClientEntity e = repo.findById(id).orElseThrow(() -> {
            log.error("Client not found id={}", id);
            return new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        });
        e.setIsActive(true);
        e = repo.save(e);
        log.info("Unarchived client id={}", id);
        return TmsClientMapper.toResponse(e);
    }

    // ------------------------------------------------------------------------
    // EXPORT
    // ------------------------------------------------------------------------

    /**
     * Export clients as CSV file.
     *
     * @param activeFilter "true", "false", or "all"
     * @param search search keyword
     * @return CSV as byte array
     */
    @Override
    public byte[] exportCsv(String activeFilter, String search) {
        log.debug("Generating CSV export | filter={}, search='{}'", activeFilter, search);

        Page<TmsClientDto> page = "all".equalsIgnoreCase(activeFilter)
                ? listAll(search, 0, Integer.MAX_VALUE)
                : getClientlist(Boolean.parseBoolean(activeFilter), search, 0, Integer.MAX_VALUE);

        String header = "id,companyName,firstName,lastName,email,isActive\n";

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
     * Escape helper for safe CSV export.
     *
     * @param s input string
     * @return CSV-safe string
     */
    private static String safe(String s) {
        return s == null ? "" : '\"' + s.replace("\"", "'") + '\"';
    }
}
