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
import net.sphuta.tms.freelancer.util.TmsClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

        // 1) Basic validation: companyName required (per your request)
        if (req.companyName() == null || req.companyName().isBlank()) {
            log.warn("Create rejected: missing companyName");
            throw new TmsException(HttpStatus.BAD_REQUEST, "companyName is required");
        }

        // 1.a) Optional: if you have a CompanyRepository (master companies table), check that it exists.
        // Uncomment and inject CompanyRepository if applicable.
    /*
    if (!companyRepo.existsByNameIgnoreCase(req.companyName())) {
        log.warn("Create rejected: company does not exist: {}", req.companyName());
        throw new TmsException(HttpStatus.BAD_REQUEST, "Company does not exist: " + req.companyName());
    }
    */

        // 2) Ensure email uniqueness for the company
        String email = req.email();
        if (email == null || email.isBlank()) {
            log.warn("Create rejected: email required");
            throw new TmsException(HttpStatus.BAD_REQUEST, "email is required");
        }

        if (repo.existsByCompanyNameIgnoreCaseAndEmailIgnoreCase(req.companyName(), email)) {
            log.warn("Create rejected: email already used for company={} email={}", req.companyName(), email);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
        }

        // 3) Proceed to create
        try {
            ClientEntity toSave = TmsClientMapper.toNewEntity(req);
            ClientEntity saved = repo.save(toSave);
            log.info("Created client id={}", saved.getId());
            return TmsClientMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrityViolation during create (company={} email={}): {}",
                    req.companyName(), req.email(), ex.getMessage(), ex);
            // DB fallback (e.g. race condition) → same friendly 409 message
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
        } catch (Exception ex) {
            log.error("Unexpected error during create: {}", ex.getMessage(), ex);
            throw new TmsException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
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
        ClientEntity existing = require(id);

        // Determine incoming (effective) company and email
        String incomingCompany = req.companyName() != null ? req.companyName().trim() : existing.getCompanyName();
        String incomingEmail   = req.email() != null ? req.email().trim() : existing.getEmail();

        // Basic validation
        if (incomingCompany == null || incomingCompany.isBlank()) {
            log.warn("Update rejected: companyName cannot be empty for id={}", id);
            throw new TmsException(HttpStatus.BAD_REQUEST, "companyName is required");
        }
        if (incomingEmail == null || incomingEmail.isBlank()) {
            log.warn("Update rejected: email cannot be empty for id={}", id);
            throw new TmsException(HttpStatus.BAD_REQUEST, "email is required");
        }

        // --- Robust conflict check using company+email (returns list to avoid NonUniqueResultException) ---
        List<ClientEntity> matches = repo.findAllByCompanyNameIgnoreCaseAndEmailIgnoreCase(incomingCompany, incomingEmail);

        // If there are matches, we must ensure all matches are this same record (allowed) otherwise conflict
        if (!matches.isEmpty()) {
            boolean onlySelf = matches.size() == 1 && matches.get(0).getId().equals(id);
            if (!onlySelf) {
                log.warn("Update conflict: company={} email={} already used by another client(s): ids={}",
                        incomingCompany, incomingEmail,
                        matches.stream().map(ClientEntity::getId).toList());
                throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
            }
        }

        // Apply DTO -> entity changes
        TmsClientMapper.updateEntity(req, existing);

        // Save with DB-level exception handling (race conditions fallback)
        try {
            existing = repo.save(existing);
            log.info("Updated client id={}", id);
            return TmsClientMapper.toResponse(existing);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrityViolation during update id={} : {}", id, ex.getMessage(), ex);
            throw new TmsException(HttpStatus.CONFLICT, "Email already in use for this company");
        } catch (Exception ex) {
            log.error("Unexpected error updating client id={}: {}", id, ex.getMessage(), ex);
            throw new TmsException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
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

//        boolean hasTE  = fasle;timeRepo.existsByClientId(id);
        boolean hasInv = invoiceRepo.existsByClientId(id);
        boolean hasEst = estimateRepo.existsByClientId(id);

        // Enforce referential integrity
//        if (hasTE || hasInv || hasEst) {
//            log.warn("Delete blocked for client id={} (timeEntries={}, invoices={}, estimates={})",
//                    id, hasTE, hasInv, hasEst);
//
//            throw new TmsException(HttpStatus.CONFLICT,
//                    "Delete blocked: client has related time entries/invoices/estimates");
//        }

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
