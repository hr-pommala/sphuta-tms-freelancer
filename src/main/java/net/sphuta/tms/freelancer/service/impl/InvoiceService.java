package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.entity.TimeEntry;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.InvoiceRepository;
import net.sphuta.tms.freelancer.repository.TimeEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * {@code InvoiceService} – Business logic for handling invoices.
 *
 * <p>This service provides functionality for creating and sending invoices. It ensures
 * domain constraints are validated (e.g., time entries must belong to the same client,
 * must be approved, and must not already be invoiced).</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Create invoices from a list of approved {@link TimeEntry} records.</li>
 *   <li>Update invoice lifecycle status (e.g., DRAFT → SENT).</li>
 *   <li>Enforce validation rules with {@link TmsException} mapped to appropriate
 *       {@link HttpStatus} codes.</li>
 *   <li>Log operations for auditing and troubleshooting.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    /** Repository for persisting and retrieving {@link Invoice} entities. */
    private final InvoiceRepository invoiceRepo;

    /** Repository for accessing {@link TimeEntry} entities associated with invoices. */
    private final TimeEntryRepository timeRepo;

    /**
     * Creates a new {@link Invoice} entity from approved {@link TimeEntry} records.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Fetch all {@link TimeEntry} records by provided IDs.</li>
     *   <li>Validate that all requested IDs exist.</li>
     *   <li>Ensure all entries belong to the same client, are {@code APPROVED},
     *       and not already linked to an invoice.</li>
     *   <li>Create and persist a new {@link Invoice} in {@code DRAFT} status.</li>
     *   <li>Associate each {@link TimeEntry} with the newly created invoice.</li>
     *   <li>Log and return the invoice.</li>
     * </ol>
     *
     * <p>Transactional Behavior:</p>
     * The method is annotated with {@link Transactional}, ensuring that if any validation fails
     * or persistence operation encounters an error, all database changes are rolled back.
     *
     * @param req DTO request containing invoice details and associated time entry IDs
     * @return the newly created {@link Invoice} entity
     * @throws TmsException if validation fails (e.g., missing entries, invalid status)
     */
    @Transactional
    public Invoice createFromEntries(TmsDto.InvoiceCreateRequest req) {
        log.debug("Creating invoice for client {} with {} time entries",
                req.clientId(), req.timeEntryIds().size());

        // Fetch all requested time entries from DB
        List<TimeEntry> entries = timeRepo.findAllById(req.timeEntryIds());

        // Validation: ensure all requested time entry IDs exist
        if (entries.size() != req.timeEntryIds().size()) {
            log.warn("Bad request: some timeEntryIds not found");
            throw new TmsException(HttpStatus.BAD_REQUEST,
                    "One or more timeEntryIds are invalid");
        }

        // Validation: ensure all entries belong to same client, APPROVED, and uninvoiced
        boolean ok = entries.stream().allMatch(te ->
                req.clientId().equals(te.getClientId()) &&
                        te.getInvoiceId() == null &&
                        te.getStatus() == TimeEntry.Status.APPROVED);

        if (!ok) {
            log.warn("Conflict: entries not approved, already invoiced, or client mismatch");
            throw new TmsException(HttpStatus.CONFLICT,
                    "Entries must be APPROVED and not already invoiced for the same client");
        }

        // Build the invoice entity in DRAFT status
        Invoice inv = Invoice.builder()
                .clientId(req.clientId())
                .issueDate(req.issueDate())
                .dueDate(req.dueDate())
                .currencyCode(req.currencyCode())
                .status(Invoice.Status.DRAFT)
                .notes(req.notes())
                .build();

        // Persist invoice
        inv = invoiceRepo.save(inv);

        // Link all time entries with the new invoice
        UUID invId = inv.getId();
        entries.forEach(te -> te.setInvoiceId(invId));
        timeRepo.saveAll(entries);

        // Log successful creation
        log.info("Created invoice {} for client {}", invId, req.clientId());
        return inv;
    }

    /**
     * Sends an invoice by updating its status from {@code DRAFT} to {@code SENT}.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Fetch the invoice by ID, throwing {@link TmsException} if not found.</li>
     *   <li>Ensure invoice is currently in {@code DRAFT} status.</li>
     *   <li>Update status to {@code SENT} and persist the change.</li>
     *   <li>Log the operation for auditing.</li>
     * </ol>
     *
     * @param invoiceId the ID of the invoice to send
     * @return the updated {@link Invoice} entity with status set to {@code SENT}
     * @throws TmsException if the invoice is not found or not in {@code DRAFT} status
     */
    @Transactional
    public Invoice send(UUID invoiceId) {
        // Fetch invoice or throw if not found
        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() ->
                        new TmsException(HttpStatus.NOT_FOUND, "Invoice not found"));

        // Validation: only DRAFT invoices can be sent
        if (inv.getStatus() != Invoice.Status.DRAFT) {
            log.warn("Invoice {} not in DRAFT; current status {}", invoiceId, inv.getStatus());
            throw new TmsException(HttpStatus.CONFLICT,
                    "Only DRAFT invoices can be sent");
        }

        // Update status and persist
        inv.setStatus(Invoice.Status.SENT);
        log.info("Invoice {} marked SENT", invoiceId);
        return invoiceRepo.save(inv);
    }
}
