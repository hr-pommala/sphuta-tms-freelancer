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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==========================================================
 * TmsClientServiceImpl
 * ==========================================================
 *
 * Service layer for managing clients.
 *
 * Responsibilities:
 * - Encapsulates business rules for client lifecycle (CRUD, archive, unarchive).
 * - Delegates persistence to {@link TmsClientRepository}.
 * - Enforces referential integrity against estimates, invoices, and time entries.
 * - Maps between entities ({@link ClientEntity}) and DTOs
 *   ({@link TmsClientDto}, {@link TmsClientDto}) using {@link TmsClientMapper}.
 *
 * Logging:
 * - DEBUG → for method entry and input parameters.
 * - INFO  → for successful CRUD operations.
 * - WARN  → for business rule violations (e.g., delete blocked).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsClientServiceImpl {

    @Autowired
    private TmsClientRepository repo;

    @Autowired
    private TmsTimeEntryRepository timeRepo;

    @Autowired
    private TmsInvoiceRepository invoiceRepo;

    @Autowired
    private TmsEstimateRepository estimateRepo;

    /**
     * Returns a paginated list of clients.
     *
     * @param active filter flag (true = active, false = archived)
     * @param search free-text search (company/name/email)
     * @param page   page number (0-based, coerced to min=0)
     * @param size   page size (coerced between 1 and 100)
     * @return page of clients mapped to response DTOs
     */
    public Page<TmsClientDto> list(boolean active, String search, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);
        log.debug("Listing clients active={}, search='{}', page={}, size={}", active, search, p, s);
        Pageable pageable = PageRequest.of(p, s);
        return repo.search(active, search, pageable).map(TmsClientMapper::toResponse);
    }

    /**
     * Ensures a client exists by ID, otherwise throws {@link TmsException}(404).
     *
     * @param id client ID
     * @return managed entity
     */
    private ClientEntity require(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new TmsException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    /**
     * Creates a new client.
     *
     * Transactional: inserts a new row and returns the mapped DTO.
     *
     * @param req request payload
     * @return created client response
     */
    @Transactional
    public TmsClientDto create(TmsClientDto req) {
        ClientEntity saved = repo.save(TmsClientMapper.toNewEntity(req));
        log.info("Created client {}", saved.getId());
        return TmsClientMapper.toResponse(saved);
    }

    /**
     * Updates an existing client (full update).
     *
     * Transactional: ensures the client exists, applies updates, saves, and returns the DTO.
     *
     * @param id  client ID
     * @param req update payload
     * @return updated client response
     */
    @Transactional
    public TmsClientDto update(Integer id, TmsClientDto req) {
        ClientEntity e = require(id);
        TmsClientMapper.updateEntity(req, e);
        e = repo.save(e);
        log.info("Updated client {}", id);
        return TmsClientMapper.toResponse(e);
    }

    /**
     * Deletes a client by ID.
     *
     * Business rules:
     * - Throws 404 if client does not exist.
     * - Throws 409 if client has related time entries, invoices, or estimates.
     *
     * @param id client ID
     */
    @Transactional
    public void delete(Integer id) {
        // ✅ ensure it exists — throws 404 via TmsException if not found
        ClientEntity e = require(id);

        boolean hasTE  = timeRepo.existsByClientId(id);
        boolean hasInv = invoiceRepo.existsByClientId(id);
        boolean hasEst = estimateRepo.existsByClientId(id);

        if (hasTE || hasInv || hasEst) {
            log.warn("409 Delete blocked for client {} (te={}, inv={}, est={})", id, hasTE, hasInv, hasEst);
            throw new TmsException(HttpStatus.CONFLICT,
                    "Delete blocked: client has related time entries/invoices/estimates");
        }

        repo.delete(e);
        log.info("Deleted client {}", id);
    }

    /**
     * Archives (deactivates) a client.
     *
     * @param id client ID
     * @return archived client response
     */
    @Transactional
    public TmsClientDto archive(Integer id) {
        ClientEntity e = require(id);
        e.setIsActive(false);
        e = repo.save(e);
        log.info("Archived client {}", id);
        return TmsClientMapper.toResponse(e);
    }

    /**
     * Unarchives (reactivates) a client.
     *
     * @param id client ID
     * @return unarchived client response
     */
    @Transactional
    public TmsClientDto unarchive(Integer id) {
        ClientEntity e = require(id);
        e.setIsActive(true);
        e = repo.save(e);
        log.info("Unarchived client {}", id);
        return TmsClientMapper.toResponse(e);
    }
}
