package net.sphuta.tms.freelancer.service.impl;

import net.sphuta.tms.freelancer.dto.TimeEntryDtos;
import net.sphuta.tms.freelancer.entity.TimeEntry;
import net.sphuta.tms.freelancer.entity.Timesheet;
import net.sphuta.tms.freelancer.exception.ApiExceptions;
import net.sphuta.tms.freelancer.repository.TimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TimesheetRepository;
import net.sphuta.tms.freelancer.service.TimeEntryService;
import net.sphuta.tms.freelancer.util.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service implementation for time entry commands (create/delete).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validate business rules (sheet mutability, date bounds, hours &gt; 0).</li>
 *   <li>Persist {@link TimeEntry} and associate with a {@link Timesheet}.</li>
 *   <li>Map entity to API DTOs for responses.</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 *   <li>Does not compute billing rules other than cost = hours × rateAtEntry (if provided).</li>
 *   <li>Deletion is blocked if the entry is considered "invoiced" (simple demo via description flag).</li>
 * </ul>
 */
@Service
@Transactional
public class TimeEntryServiceImpl implements TimeEntryService {

    /** Class logger (SLF4J) for method-level diagnostics and audits. */
    private static final Logger log = LoggerFactory.getLogger(TimeEntryServiceImpl.class);

    /** Repository for {@link TimeEntry} persistence and retrieval. */
    private final TimeEntryRepository entryRepo;

    /** Repository for {@link Timesheet} persistence and retrieval. */
    private final TimesheetRepository timesheetRepo;

    /**
     * Constructs the service with required repositories.
     *
     * @param e time entry repository (required)
     * @param t timesheet repository (required)
     */
    public TimeEntryServiceImpl(TimeEntryRepository e, TimesheetRepository t) {
        this.entryRepo = e;
        this.timesheetRepo = t;
    }

    /**
     * Create a new {@link TimeEntry} attached to a {@link Timesheet}.
     * <p>
     * Validation performed:
     * <ol>
     *   <li>Timesheet exists.</li>
     *   <li>Timesheet status is mutable (not LOCKED).</li>
     *   <li>Entry date is within the timesheet period.</li>
     *   <li>Hours &gt; 0.</li>
     * </ol>
     *
     * @param req request payload containing timesheetId, entryDate, description, hours, rateAtEntry
     * @return mapped API response DTO reflecting the created entry
     * @throws ApiExceptions.NotFoundException   if the timesheet does not exist
     * @throws ApiExceptions.ConflictException   if the timesheet is LOCKED
     * @throws ApiExceptions.ValidationException if business rules fail (date range / hours)
     */
    @Override
    public TimeEntryDtos.TimeEntryResponse create(TimeEntryDtos.TimeEntryCreateRequest req) {
        // --- Logging: entry with key request fields (guard against PII; include identifiers only) ---
        log.info("TimeEntry#create requested: timesheetId={}, entryDate={}, hours={}, rateAtEntry={}",
                req.timesheetId(), req.entryDate(), req.hours(), req.rateAtEntry());

        // Fetch parent timesheet or fail with 404
        Timesheet t = timesheetRepo.findById(req.timesheetId())
                .orElseThrow(() -> {
                    log.warn("TimeEntry#create: timesheet not found: {}", req.timesheetId());
                    return new ApiExceptions.NotFoundException("Timesheet not found");
                });

        // Enforce mutability (LOCKED => 409)
        if (!t.getStatus().isMutable()) {
            log.warn("TimeEntry#create blocked: timesheet={} status={} (LOCKED)", t.getId(), t.getStatus());
            throw new ApiExceptions.ConflictException("Timesheet is LOCKED and cannot be modified");
        }

        // Validate entryDate within [periodStart, periodEnd]
        if (req.entryDate().isBefore(t.getPeriodStart()) || req.entryDate().isAfter(t.getPeriodEnd())) {
            log.warn("TimeEntry#create validation failed: entryDate {} outside [{}, {}] for timesheet={}",
                    req.entryDate(), t.getPeriodStart(), t.getPeriodEnd(), t.getId());
            throw new ApiExceptions.ValidationException("entryDate outside timesheet period");
        }

        // Validate hours > 0
        if (req.hours() == null || req.hours().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("TimeEntry#create validation failed: hours={} must be > 0 for timesheet={}",
                    req.hours(), t.getId());
            throw new ApiExceptions.ValidationException("hours must be > 0");
        }

        // Compute cost if rate present (no side effects on hours)
        var cost = req.rateAtEntry() == null ? null : req.rateAtEntry().multiply(req.hours());
        log.debug("TimeEntry#create: computed cost={} (hours={} × rateAtEntry={})",
                cost, req.hours(), req.rateAtEntry());

        // --- Build the new entity (builder pattern) ---
        var e = TimeEntry.builder()
                .timesheet(t)                      // link to parent timesheet
                .entryDate(req.entryDate())        // work date (validated)
                .description(req.description())    // optional, capped by DTO validation
                .hours(req.hours())                // validated (> 0)
                .rateAtEntry(req.rateAtEntry())    // optional rate snapshot
                .costAtEntry(cost)                 // optional cost snapshot
                .build();

        // Persist entry and attach to timesheet aggregate
        entryRepo.save(e);
        t.getEntries().add(e);

        // Success log with identifiers
        log.info("TimeEntry#create success: entryId={} timesheetId={}", e.getId(), t.getId());

        // Map to API response and return
        return Mappers.toEntryResponse(e);
    }

    /**
     * Delete a {@link TimeEntry} by id.
     * <p>
     * Business rule: deletion is blocked (409) if the entry is considered already invoiced.
     *
     * @param id time entry identifier
     * @throws ApiExceptions.NotFoundException if the entry does not exist
     * @throws ApiExceptions.ConflictException if the entry is flagged as "invoiced"
     */
    @Override
    public void delete(UUID id) {
        // --- Logging: entry with id ---
        log.info("TimeEntry#delete requested: entryId={}", id);

        // Load entry or 404
        var e = entryRepo.findById(id).orElseThrow(() -> {
            log.warn("TimeEntry#delete: entry not found: {}", id);
            return new ApiExceptions.NotFoundException("Time entry not found");
        });

        // Business rule: prevent delete if invoiced (simple demo via description flag)
        if (e.getDescription() != null && e.getDescription().toLowerCase().contains("[invoiced]")) {
            log.warn("TimeEntry#delete blocked: entryId={} marked as invoiced", id);
            throw new ApiExceptions.ConflictException("Time entry already invoiced; cannot delete");
        }

        // Perform deletion
        entryRepo.delete(e);
        log.info("TimeEntry#delete success: entryId={}", id);
    }
}
