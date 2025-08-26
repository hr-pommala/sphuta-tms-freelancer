package net.sphuta.tms.freelancer.service.impl;


import net.sphuta.tms.freelancer.dto.TimeEntryDtos;
import net.sphuta.tms.freelancer.dto.TimesheetDtos;
import net.sphuta.tms.freelancer.enam.TimesheetStatus; // NOTE: package name as provided (no change)
import net.sphuta.tms.freelancer.entity.Timesheet;
import net.sphuta.tms.freelancer.entity.TimeEntry;
import net.sphuta.tms.freelancer.exception.ApiExceptions;
import net.sphuta.tms.freelancer.repository.TimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TimesheetRepository;
import net.sphuta.tms.freelancer.response.PageResponse;
import net.sphuta.tms.freelancer.service.TimesheetService;
import net.sphuta.tms.freelancer.util.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service implementation for Timesheet operations.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>List timesheets with filters and pagination</li>
 *   <li>Create a new timesheet header</li>
 *   <li>Fetch detailed timesheet with entries</li>
 *   <li>Submit (auto-approve) a timesheet</li>
 *   <li>Bulk upsert time entries into a given timesheet</li>
 *   <li>Lock a timesheet (prevents further modifications)</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 *   <li>All business validations and error mapping are preserved; only comments and logs were added.</li>
 *   <li>{@code @Transactional} is applied at class level; read-only is specified where appropriate.</li>
 * </ul>
 */
@Service
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    /** Logger for this service. */
    private static final Logger log = LoggerFactory.getLogger(TimesheetServiceImpl.class);

    /** Repository for timesheet aggregates (headers + lazy entries). */
    private final TimesheetRepository timesheetRepo;

    /** Repository for individual time entries. */
    private final TimeEntryRepository entryRepo;

    /**
     * Constructor injection for required repositories.
     *
     * @param t timesheet repository
     * @param e time entry repository
     */
    public TimesheetServiceImpl(TimesheetRepository t, TimeEntryRepository e) {
        this.timesheetRepo = t;
        this.entryRepo = e;
    }

    /**
     * List timesheets using optional filters and pagination.
     *
     * @param f filters capturing project, status, date range, and page/size
     * @return a {@link PageResponse} of {@link TimesheetDtos.TimesheetSummary}
     */
    @Override
    public PageResponse<TimesheetDtos.TimesheetSummary> list(TimesheetDtos.TimesheetFilters f) {
        // --- log method entry with key parameters (defensive null handling for logs only)
        log.debug("Timesheet list called with filters: projectId={}, status={}, from={}, to={}, page={}, size={}",
                f.projectId(), f.status(), f.from(), f.to(), f.page(), f.size());

        // Derive paging defaults without changing behavior
        int page = f.page() == null ? 0 : f.page();
        int size = f.size() == null ? 25 : f.size();
        var pageable = PageRequest.of(page, size);

        // For date range, maintain existing default semantics
        var from = f.from() == null ? LocalDate.MIN : f.from();
        var to = f.to() == null ? LocalDate.MAX : f.to();

        // --- decide which finder to use based on provided filters
        if (f.projectId() != null && f.status() != null) {
            log.trace("Using finder: findByProjectIdAndStatusAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual");
        } else if (f.projectId() != null) {
            log.trace("Using finder: findByProjectId");
        } else if (f.status() != null) {
            log.trace("Using finder: findByStatus");
        } else {
            log.trace("Using finder: findAll");
        }

        // Branch selection preserved exactly as original
        var p = (f.projectId() != null && f.status() != null)
                ? timesheetRepo.findByProjectIdAndStatusAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
                f.projectId(), f.status(), from, to, pageable)
                : (f.projectId() != null)
                ? timesheetRepo.findByProjectId(f.projectId(), pageable)
                : (f.status() != null)
                ? timesheetRepo.findByStatus(f.status(), pageable)
                : timesheetRepo.findAll(pageable);

        // Map to summaries and build page response
        var content = p.map(Mappers::toSummary).getContent();
        var response = new PageResponse<>(
                content,
                new PageResponse.PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages())
        );

        log.debug("Timesheet list result: pageNumber={}, pageSize={}, totalElements={}, totalPages={}",
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());

        return response;
    }

    /**
     * Create a new timesheet header for a project and period.
     *
     * @param req creation request containing projectId, periodStart, periodEnd
     * @return detailed timesheet view of the created entity
     */
    @Override
    public TimesheetDtos.TimesheetDetail create(TimesheetDtos.TimesheetCreateRequest req) {
        log.info("Create timesheet requested: projectId={}, periodStart={}, periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        // Validate period range (behavior unchanged)
        if (req.periodEnd().isBefore(req.periodStart())) {
            log.warn("Create timesheet validation failed: periodEnd < periodStart (start={}, end={})",
                    req.periodStart(), req.periodEnd());
            throw new ApiExceptions.ValidationException("periodEnd must be >= periodStart");
        }

        // Enforce uniqueness per project + period
        timesheetRepo.findByProjectIdAndPeriodStartAndPeriodEnd(req.projectId(), req.periodStart(), req.periodEnd())
                .ifPresent(t -> {
                    log.warn("Create timesheet conflict: projectId={} already has timesheet for {}..{} (id={})",
                            req.projectId(), req.periodStart(), req.periodEnd(), t.getId());
                    throw new ApiExceptions.ConflictException("Timesheet for project & period already exists");
                });

        // Build and persist entity (status defaults to DRAFT)
        Timesheet t = Timesheet.builder()
                .projectId(req.projectId())
                .periodStart(req.periodStart())
                .periodEnd(req.periodEnd())
                .status(TimesheetStatus.DRAFT)
                .build();

        timesheetRepo.save(t);
        log.info("Created timesheet id={} for projectId={} ({}..{})",
                t.getId(), t.getProjectId(), t.getPeriodStart(), t.getPeriodEnd());

        // Map to detail DTO and return
        var detail = Mappers.toDetail(t);
        log.debug("Create timesheet response prepared for id={}", t.getId());
        return detail;
    }

    /**
     * Fetch a single timesheet detail (header + entries + totals).
     *
     * @param id timesheet id
     * @return detailed view
     */
    @Override
    @Transactional(readOnly = true)
    public TimesheetDtos.TimesheetDetail get(UUID id) {
        log.debug("Get timesheet requested: id={}", id);

        // Load or 404
        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Timesheet not found: id={}", id);
                    return new ApiExceptions.NotFoundException("Timesheet not found");
                });

        // Ensure entries are loaded (preserve original lazy-load trigger)
        t.getEntries().size();
        log.trace("Loaded {} entries for timesheet id={}", t.getEntries().size(), id);

        var detail = Mappers.toDetail(t);
        log.debug("Get timesheet response ready: id={}, status={}", id, t.getStatus());
        return detail;
    }

    /**
     * Submit a timesheet (auto-approve; idempotent semantics at application level).
     *
     * @param id timesheet id
     * @return detailed view (now APPROVED)
     */
    @Override
    public TimesheetDtos.TimesheetDetail submit(UUID id) {
        log.info("Submit (approve) timesheet requested: id={}", id);

        // Load or 404
        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Submit failed: timesheet not found: id={}", id);
                    return new ApiExceptions.NotFoundException("Timesheet not found");
                });

        // Set status to APPROVED (behavior unchanged)
        t.setStatus(TimesheetStatus.APPROVED);
        log.info("Timesheet approved: id={}", id);

        var detail = Mappers.toDetail(t);
        log.debug("Submit response ready for id={}", id);
        return detail;
    }

    /**
     * Bulk upsert time entries for a given timesheet.
     * <p>
     * Insert behavior: creates new entries when none exist for (entryDate, description).<br/>
     * Update behavior: updates hours/rate/cost when an existing entry is found.<br/>
     * Locked timesheets reject modifications with 409.
     *
     * @param timesheetId target timesheet id
     * @param req bulk payload containing rows to upsert
     * @return aggregate result including inserted/updated counts and total hours
     */
    @Override
    public TimeEntryDtos.BulkUpsertResponse bulkUpsert(UUID timesheetId, TimeEntryDtos.BulkUpsertRequest req) {
        log.info("Bulk upsert requested: timesheetId={}, rows={}",
                timesheetId, (req.entries() == null ? 0 : req.entries().size()));

        // Load timesheet or 404
        var t = timesheetRepo.findById(timesheetId)
                .orElseThrow(() -> {
                    log.warn("Bulk upsert failed: timesheet not found: id={}", timesheetId);
                    return new ApiExceptions.NotFoundException("Timesheet not found");
                });

        // Enforce mutability
        if (!t.getStatus().isMutable()) {
            log.warn("Bulk upsert blocked: timesheet is LOCKED (id={})", timesheetId);
            throw new ApiExceptions.ConflictException("Timesheet is LOCKED and cannot be modified");
        }

        int inserted = 0, updated = 0;

        // Process each entry in request exactly as original logic
        for (var r : req.entries()) {
            // Validate period bounds
            if (r.entryDate().isBefore(t.getPeriodStart()) || r.entryDate().isAfter(t.getPeriodEnd())) {
                log.warn("Row invalid (outside period): date={}, period={}..{}", r.entryDate(), t.getPeriodStart(), t.getPeriodEnd());
                throw new ApiExceptions.ValidationException("Some entries invalid: outside period");
            }
            // Validate hours > 0
            if (r.hours() == null || r.hours().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Row invalid (hours must be > 0): hours={}", r.hours());
                throw new ApiExceptions.ValidationException("Some entries invalid: hours must be > 0");
            }

            // Lookup existing row by (timesheet, entryDate, description)
            var existing = entryRepo.findByTimesheetAndEntryDateAndDescription(t, r.entryDate(), r.description()).orElse(null);

            if (existing == null) {
                // INSERT path
                var e = buildEntryFromReq(t, r);
                entryRepo.save(e);
                t.getEntries().add(e);
                inserted++;
                log.trace("Inserted entry: date={}, desc='{}', hours={}", r.entryDate(), r.description(), r.hours());
            } else {
                // UPDATE path – preserve functionality exactly
                existing.setHours(r.hours());
                existing.setRateAtEntry(r.rateAtEntry());
                existing.setCostAtEntry(r.rateAtEntry() == null ? null : r.rateAtEntry().multiply(r.hours()));
                updated++;
                log.trace("Updated entry: date={}, desc='{}', newHours={}, newRate={}", r.entryDate(), r.description(), r.hours(), r.rateAtEntry());
            }
        }

        // Recompute total hours using existing mapper helper
        var total = Mappers.totalHours(t);

        log.info("Bulk upsert completed for timesheetId={} (inserted={}, updated={}, totalHours={})",
                timesheetId, inserted, updated, total);

        return new TimeEntryDtos.BulkUpsertResponse(inserted, updated, 0, total);
    }

    /**
     * Build a new {@code TimeEntry} from the upsert row request.
     * <p>Note: No functional change; extracted logging is intentionally omitted here to keep hot path lean.</p>
     *
     * @param t timesheet aggregate (must be mutable)
     * @param r incoming row
     * @return a new, unsaved {@code TimeEntry}
     */
    private TimeEntry buildEntryFromReq(
            Timesheet t,
            TimeEntryDtos.TimeEntryCreateRequest r
    ) {
        // Compute cost only if rate provided (preserves original logic)
        var cost = r.rateAtEntry() == null ? null : r.rateAtEntry().multiply(r.hours());

        // Build entry with Lombok builder (unchanged fields)
        return TimeEntry.builder()
                .timesheet(t)
                .entryDate(r.entryDate())
                .description(r.description())
                .hours(r.hours())
                .rateAtEntry(r.rateAtEntry())
                .costAtEntry(cost)
                .build();
    }

    /**
     * Lock a timesheet so it cannot be modified.
     *
     * @param id timesheet id
     */
    @Override
    public void lock(UUID id) {
        log.info("Lock timesheet requested: id={}", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lock failed: timesheet not found: id={}", id);
                    return new ApiExceptions.NotFoundException("Timesheet not found");
                });

        // Apply locked status (no other side effects)
        t.setStatus(TimesheetStatus.LOCKED);
        log.info("Timesheet locked: id={}", id);
    }
}
