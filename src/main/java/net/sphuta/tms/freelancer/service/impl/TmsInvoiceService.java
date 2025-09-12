package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsInvoiceDto;
import net.sphuta.tms.freelancer.entity.InvoiceEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.TmsInvoiceRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.util.TmsInvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ==========================================================
 * TmsInvoiceService
 * ==========================================================
 *
 * Service layer handling **invoice lifecycle** operations.
 *
 * Responsibilities:
 * - Create invoices from approved, uninvoiced time entries for a single client.
 * - Transition invoice status (e.g., DRAFT → SENT).
 *
 * Business rules enforced here:
 * - All provided timeEntryIds must exist.
 * - All entries must belong to the same client as the request.
 * - Entries must be APPROVED and not already linked to an invoice.
 * - Only DRAFT invoices can be SENT.
 *
 * Logging:
 * - DEBUG for inputs and decisions.
 * - WARN for rule violations (bad request / conflict).
 * - INFO for successful state changes (created/sent).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsInvoiceService {

    @Autowired
    private TmsInvoiceRepository invoiceRepo;

    @Autowired
    private TmsTimeEntryRepository timeRepo;

    /**
     * Create a new DRAFT invoice from selected time entries.
     *
     * Steps:
     * 1) Load all requested time entries and ensure they all exist.
     * 2) Validate they belong to the same client, are APPROVED, and are not invoiced.
     * 3) Create and persist a DRAFT invoice; link entries to the invoice.
     *
     * Errors:
     * - 400 Bad Request if any timeEntryId is invalid.
     * - 409 Conflict if entries are not APPROVED, already invoiced, or client mismatched.
     */
//    @Transactional
//    public TmsInvoiceDto createFromEntries(TmsInvoiceDto req) {
//        // ---- entry log + (optional) timing ----
//        log.debug("Creating invoice for client {} with {} time entries",
//                req.clientId(), req.timeEntryIds().size());
//        long _startNs = System.nanoTime();
//
//        // ---- fetch and validate requested time entries ----
//        List<TimeEntryEntity> entries = timeRepo.findAllById(req.timeEntryIds());
//
//        // Ensure every requested id exists
//        if (entries.size() != req.timeEntryIds().size()) {
//            log.warn("Bad request: some timeEntryIds not found");
//            throw new TmsException(HttpStatus.BAD_REQUEST,
//                    "One or more timeEntryIds are invalid");
//        }
//
//        // Validate client match, not already invoiced, and APPROVED status
//        boolean ok = entries.stream().allMatch(te ->
//                req.clientId().equals(te.getClientId()) &&
//                        te.getInvoiceId() == null &&
//                        te.getStatus() == TimeEntryEntity.Status.APPROVED);
//
//        if (!ok) {
//            log.warn("Conflict: entries not approved, already invoiced, or client mismatch");
//            throw new TmsException(HttpStatus.CONFLICT,
//                    "Entries must be APPROVED and not already invoiced for the same client");
//        }
//
//        // ---- create invoice entity in DRAFT ----
//        InvoiceEntity inv = TmsInvoiceMapper.toNewDraft(req);
//        inv = invoiceRepo.save(inv);
//
//        // ---- link all entries to the new invoice ----
//        Integer invId = inv.getId();
//        entries.forEach(te -> te.setInvoiceId(invId));
//        timeRepo.saveAll(entries);
//
//        // ---- success + timing log ----
//        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
//        log.info("Created invoice {} for client {} ({} ms)", invId, req.clientId(), _tookMs);
//
//        return TmsInvoiceMapper.toResponse(inv);
//    }

    /**
     * Send/issue an invoice (transition DRAFT → SENT).
     *
     * Errors:
     * - 404 Not Found if the invoice does not exist.
     * - 409 Conflict if current status is not DRAFT.
     */
    @Transactional
    public TmsInvoiceDto send(Integer invoiceId) {
        // ---- entry log + (optional) timing ----
        log.debug("Attempting to send invoice {}", invoiceId);
        long _startNs = System.nanoTime();

        // ---- load invoice or 404 ----
        InvoiceEntity inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new TmsException(HttpStatus.NOT_FOUND, "Invoice not found"));

        // ---- enforce status rule ----
        if (inv.getStatus() != InvoiceEntity.Status.DRAFT) {
            log.warn("Invoice {} not in DRAFT; current status {}", invoiceId, inv.getStatus());
            throw new TmsException(HttpStatus.CONFLICT, "Only DRAFT invoices can be sent");
        }

        // ---- transition to SENT ----
        inv.setStatus(InvoiceEntity.Status.SENT);
        inv = invoiceRepo.save(inv);

        // ---- success + timing log ----
        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
        log.info("Invoice {} marked SENT ({} ms)", invoiceId, _tookMs);

        return TmsInvoiceMapper.toResponse(inv);
    }
}
