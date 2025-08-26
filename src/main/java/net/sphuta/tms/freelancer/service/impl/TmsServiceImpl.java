package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.TmsEntity;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.EstimateRepository;
import net.sphuta.tms.freelancer.repository.InvoiceRepository;
import net.sphuta.tms.freelancer.repository.TimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code TmsServiceImpl} – Business logic for managing Clients ({@link TmsEntity}).
 *
 * <p>This service encapsulates all client-related operations, including:</p>
 * <ul>
 *   <li>Listing clients with search & pagination support.</li>
 *   <li>CRUD operations (create, update, delete).</li>
 *   <li>Archiving/unarchiving clients.</li>
 *   <li>Enforcing business rules such as preventing deletion if
 *       invoices, estimates, or time entries exist.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsServiceImpl {

    /** Repository for client entities. */
    private final TmsRepository repo;
    /** Repository for time entries (used for deletion checks). */
    private final TimeEntryRepository timeRepo;
    /** Repository for invoices (used for deletion checks). */
    private final InvoiceRepository invoiceRepo;
    /** Repository for estimates (used for deletion checks). */
    private final EstimateRepository estimateRepo;

    /**
     * List clients with filtering and pagination.
     *
     * @param active whether to include only active clients
     * @param search free-text search string
     * @param page   page number (0-based, defaults to 0 if invalid)
     * @param size   page size (bounded between 1 and 100)
     * @return a page of clients matching the criteria
     */
    public Page<TmsEntity> list(boolean active, String search, int page, int size) {
        int p = Math.max(page, 0);                     // ensure non-negative
        int s = Math.min(Math.max(size, 1), 100);      // clamp size to [1, 100]
        log.debug("Listing clients active={}, search='{}', page={}, size={}", active, search, p, s);
        Pageable pageable = PageRequest.of(p, s);
        return repo.search(active, search, pageable);
    }

    /**
     * Find a client by ID.
     *
     * @param id client identifier
     * @return optional client entity
     */
    public Optional<TmsEntity> find(UUID id) {
        return repo.findById(id);
    }

    /**
     * Create a new client from a DTO request.
     *
     * @param req DTO request containing client details
     * @return persisted client entity
     */
    @Transactional
    public TmsEntity create(TmsDto.ClientRequest req) {
        TmsEntity e = map(req, new TmsEntity());

        // Defaults and flags
        e.setSendReminders(Boolean.TRUE.equals(req.sendReminders()));
        e.setChargeLateFees(Boolean.TRUE.equals(req.chargeLateFees()));
        e.setAllowInvoiceAttachments(Boolean.TRUE.equals(req.allowInvoiceAttachments()));
        e.setActive(req.isActive() == null || req.isActive());

        TmsEntity saved = repo.save(e);
        log.info("Created client {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing client.
     *
     * @param id  client identifier
     * @param req DTO request with updated client details
     * @return updated client entity
     * @throws TmsException if client is not found
     */
    @Transactional
    public TmsEntity update(UUID id, TmsDto.ClientRequest req) {
        TmsEntity e = repo.findById(id).orElseThrow(() ->
                new TmsException(HttpStatus.NOT_FOUND, "Client not found"));

        // Update fields
        map(req, e);
        if (req.sendReminders() != null) e.setSendReminders(req.sendReminders());
        if (req.chargeLateFees() != null) e.setChargeLateFees(req.chargeLateFees());
        if (req.allowInvoiceAttachments() != null) e.setAllowInvoiceAttachments(req.allowInvoiceAttachments());
        if (req.isActive() != null) e.setActive(req.isActive());

        log.info("Updated client {}", id);
        return repo.save(e);
    }

    /**
     * Delete a client if it has no related time entries, invoices, or estimates.
     *
     * @param id client identifier
     * @throws TmsException if client has related records (HTTP 409 Conflict)
     */
    @Transactional
    public void delete(UUID id) {
        boolean hasTE  = timeRepo.existsByClientId(id);
        boolean hasInv = invoiceRepo.existsByClientId(id);
        boolean hasEst = estimateRepo.existsByClientId(id);

        if (hasTE || hasInv || hasEst) {
            log.warn("409 Delete blocked for client {} (te={}, inv={}, est={})", id, hasTE, hasInv, hasEst);
            throw new TmsException(HttpStatus.CONFLICT,
                    "Delete blocked: client has related time entries/invoices/estimates");
        }

        repo.deleteById(id);
        log.info("Deleted client {}", id);
    }

    /**
     * Archive a client (mark inactive).
     *
     * @param id client identifier
     * @return updated client entity
     * @throws TmsException if client is not found
     */
    @Transactional
    public TmsEntity archive(UUID id) {
        TmsEntity e = repo.findById(id).orElseThrow(() ->
                new TmsException(HttpStatus.NOT_FOUND, "Client not found"));
        e.setActive(false);
        log.info("Archived client {}", id);
        return repo.save(e);
    }

    /**
     * Unarchive a client (mark active).
     *
     * @param id client identifier
     * @return updated client entity
     * @throws TmsException if client is not found
     */
    @Transactional
    public TmsEntity unarchive(UUID id) {
        TmsEntity e = repo.findById(id).orElseThrow(() ->
                new TmsException(HttpStatus.NOT_FOUND, "Client not found"));
        e.setActive(true);
        log.info("Unarchived client {}", id);
        return repo.save(e);
    }

    /**
     * Map request fields onto an entity (null-safe setter).
     *
     * @param r client request DTO
     * @param e entity to populate
     * @return updated entity
     */
    private TmsEntity map(TmsDto.ClientRequest r, TmsEntity e) {
        if (r.companyName()   != null) e.setCompanyName(r.companyName());
        if (r.firstName()     != null) e.setFirstName(r.firstName());
        if (r.lastName()      != null) e.setLastName(r.lastName());
        if (r.email()         != null) e.setEmail(r.email());
        if (r.mobilePhone()   != null) e.setMobilePhone(r.mobilePhone());
        if (r.businessPhone() != null) e.setBusinessPhone(r.businessPhone());
        if (r.addressLine1()  != null) e.setAddressLine1(r.addressLine1());
        if (r.addressLine2()  != null) e.setAddressLine2(r.addressLine2());
        if (r.city()          != null) e.setCity(r.city());
        if (r.state()         != null) e.setState(r.state());
        if (r.postalCode()    != null) e.setPostalCode(r.postalCode());
        if (r.countryCode()   != null) e.setCountryCode(r.countryCode());
        if (r.lateFeePercent()!= null) e.setLateFeePercent(r.lateFeePercent());
        if (r.currencyCode()  != null) e.setCurrencyCode(r.currencyCode());
        if (r.language()      != null) e.setLanguage(r.language());
        return e;
    }
}
