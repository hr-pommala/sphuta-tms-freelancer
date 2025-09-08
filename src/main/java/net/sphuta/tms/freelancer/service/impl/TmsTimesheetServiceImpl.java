package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.BulkUpsertDto;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.dto.TmsTimesheetDto;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.exception.ApiExceptions;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.service.TmsTimesheetService;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link TmsTimesheetService} for managing Timesheets.
 * <p>
 * Provides business logic for creating, retrieving, submitting,
 * bulk-upserting, and locking timesheets. All database operations
 * are wrapped in transactional boundaries.
 * </p>
 */
@Slf4j
@Service
@Transactional
public class TmsTimesheetServiceImpl implements TmsTimesheetService {

    /** Repository for Timesheet entities */
    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    /** Repository for Time Entry entities */
    @Autowired
    private TmsTimeEntryRepository entryRepo;

    /**
     * Repository used only for existence check of projects.
     * Replace with your actual project client/repository if needed.
     */
    @Autowired
    private TmsProjectRepository projectRepository;

    /**
     * Create a new timesheet for a project and period.
     *
     * @param req DTO containing project and period details
     * @return created {@link TmsTimesheetDto}
     */
    @Override
    public TmsTimesheetDto create(TmsTimesheetDto req) {
        log.info("Request received to create timesheet: projectId={}, periodStart={}, periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        // Validate period consistency
        if (req.periodEnd().isBefore(req.periodStart())) {
            log.error("Validation failed: periodEnd={} is before periodStart={}", req.periodEnd(), req.periodStart());
            throw new ApiExceptions.ValidationException("periodEnd must be >= periodStart");
        }

        // NEW: verify project exists (throw if not)
        if (req.projectId() == null || !projectRepository.existsById(req.projectId())) {
            log.error("Project not found: projectId={}", req.projectId());
            throw new ApiExceptions.NotFoundException("Project not found");
        }

        // Check for duplicate timesheet
        timesheetRepo.findByProjectIdAndPeriodStartAndPeriodEnd(req.projectId(), req.periodStart(), req.periodEnd())
                .ifPresent(t -> {
                    log.error("Conflict: Timesheet already exists for projectId={} period {}..{}",
                            req.projectId(), req.periodStart(), req.periodEnd());
                    throw new ConflictException("Timesheet for project & period already exists");
                });

        // Build and save new timesheet entity
        TimesheetEntity t = TimesheetEntity.builder()
                .projectId(req.projectId())
                .periodStart(req.periodStart())
                .periodEnd(req.periodEnd())
                .status(TimesheetStatus.DRAFT)
                .build();

        timesheetRepo.save(t);
        log.info("Created new timesheet id={} projectId={} {}..{}", t.getId(), t.getProjectId(),
                t.getPeriodStart(), t.getPeriodEnd());

        return TmsTimesheetMappers.toDetail(t);
    }

    /**
     * Retrieve a timesheet by ID with its details.
     *
     * @param id timesheet identifier
     * @return {@link TmsTimesheetDto} with entries and totals
     */
    @Override
    @Transactional(readOnly = true)
    public TmsTimesheetDto get(Integer id) {
        log.info("Fetching timesheet by id={}", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Timesheet not found: id={}", id);
                    return new NotFoundException("Timesheet not found");
                });

        // Force load entries from lazy collection
        log.debug("Loading entries for timesheet id={}", id);
        t.getEntries().size();

        log.info("Successfully fetched timesheet id={} with {} entries", id, t.getEntries().size());
        return TmsTimesheetMappers.toDetail(t);
    }

    /**
     * Submit a timesheet for approval.
     *
     * @param id timesheet identifier
     * @return updated {@link TmsTimesheetDto} with APPROVED status
     */
    @Override
    public TmsTimesheetDto submit(Integer id) {
        log.info("Submitting timesheet id={} for approval", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Timesheet not found during submit: id={}", id);
                    return new NotFoundException("Timesheet not found");
                });

        t.setStatus(TimesheetStatus.APPROVED);
        log.info("Timesheet approved: id={} status={}", id, t.getStatus());

        return TmsTimesheetMappers.toDetail(t);
    }

    /**
     * Bulk upsert time entries in a timesheet.
     *
     * <p>This method inserts or updates time entries for a given timesheet.</p>
     * <ul>
     *   <li>Validates request payload (date range, hours > 0)</li>
     *   <li>Checks if the timesheet exists and is mutable</li>
     *   <li>Decides whether to insert or update each entry</li>
     *   <li>Returns a response DTO with operation summary</li>
     * </ul>
     *
     * @param timesheetId timesheet identifier
     * @param req DTO containing entries and mode (UPSERT, INSERT_ONLY, UPDATE_ONLY)
     * @return {@link BulkUpsertDto} containing inserted/updated counts and processed entries
     */
    @Override
    public BulkUpsertDto bulkUpsert(Integer timesheetId, BulkUpsertDto req) {
        log.info("Bulk upsert requested: timesheetId={}, rows={}",
                timesheetId, (req.entries() == null ? 0 : req.entries().size()));

        // If request has no entries, log and return an empty response
        if (req.entries() == null || req.entries().isEmpty()) {
            log.warn("No entries provided for bulk upsert");
            return TmsTimesheetMappers.toBulkUpsertResponse(List.of(), req.mode(), 0, 0, 0, BigDecimal.ZERO);
        }

        // Fetch the timesheet or throw NOT_FOUND exception
        var t = timesheetRepo.findById(timesheetId)
                .orElseThrow(() -> {
                    log.error("Timesheet not found: id={}", timesheetId);
                    return new NotFoundException("Timesheet not found");
                });

        // Check mutability of timesheet
        if (!t.getStatus().isMutable()) {
            log.error("Timesheet is LOCKED and cannot be modified: id={}", timesheetId);
            throw new ConflictException("Timesheet is LOCKED and cannot be modified");
        }

        int inserted = 0, updated = 0;
        List<TimeEntryDto> processed = new ArrayList<>();

        // Process each incoming request entry
        for (var r : req.entries()) {
            log.debug("Processing entry: date={}, desc='{}', hours={}, rate={}",
                    r.entryDate(), r.description(), r.hours(), r.rateAtEntry());

            // Validation: entry must be within period range
            if (r.entryDate().isBefore(t.getPeriodStart()) || r.entryDate().isAfter(t.getPeriodEnd())) {
                log.warn("Validation failed: entry outside period. entry={}, period={}..{}",
                        r.entryDate(), t.getPeriodStart(), t.getPeriodEnd());
                throw new ApiExceptions.ValidationException("Some entries invalid: outside period");
            }

            // Validation: hours must be > 0
            if (r.hours() == null || r.hours().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Validation failed: entry with invalid hours. entry={}", r);
                throw new ApiExceptions.ValidationException("Some entries invalid: hours must be > 0");
            }

            // Lookup existing entry by (timesheetId + entryDate + description)
            var existing = entryRepo.findByTimesheetAndEntryDateAndDescription(
                    t, r.entryDate(), r.description()).orElse(null);

            if (existing == null) {
                // INSERT path
                var e = buildEntryFromReq(t, r);
                entryRepo.save(e);
                t.getEntries().add(e);
                inserted++;
                processed.add(TmsTimesheetMappers.toTimeEntryDto(e));
                log.info("Inserted new entry: date={}, desc='{}', hours={}, rate={}",
                        r.entryDate(), r.description(), r.hours(), r.rateAtEntry());
            } else {
                // UPDATE path
                existing.setHours(r.hours());
                existing.setRateAtEntry(r.rateAtEntry());
                existing.setCostAtEntry(r.rateAtEntry() == null ? null : r.rateAtEntry().multiply(r.hours()));
                updated++;
                processed.add(TmsTimesheetMappers.toTimeEntryDto(existing));
                log.info("Updated existing entry: id={}, date={}, desc='{}', newHours={}, newRate={}",
                        existing.getId(), r.entryDate(), r.description(), r.hours(), r.rateAtEntry());
            }
        }

        // Compute total hours after all operations
        BigDecimal total = TmsTimesheetMappers.totalHours(t);
        log.debug("Total hours recomputed for timesheetId={} -> {}", timesheetId, total);

        // Build response DTO
        var response = TmsTimesheetMappers.toBulkUpsertResponse(processed, req.mode(), inserted, updated, 0, total);
        log.info("Bulk upsert completed: timesheetId={}, inserted={}, updated={}, totalHours={}",
                timesheetId, inserted, updated, total);

        return response;
    }


    /**
     * Build {@link TimeEntryEntity} from request DTO.
     *
     * @param t Timesheet entity
     * @param r Time entry DTO
     * @return populated {@link TimeEntryEntity}
     */
    private TimeEntryEntity buildEntryFromReq(TimesheetEntity t, TimeEntryDto r) {
        var cost = r.rateAtEntry() == null ? null : r.rateAtEntry().multiply(r.hours());
        var entry = TimeEntryEntity.builder()
                .timesheet(t)
                .entryDate(r.entryDate())
                .description(r.description())
                .hours(r.hours())
                .rateAtEntry(r.rateAtEntry())
                .costAtEntry(cost)
                .build();

        log.debug("Built TimeEntryEntity for timesheetId={} date={} hours={} cost={}",
                t.getId(), r.entryDate(), r.hours(), cost);
        return entry;
    }

    /**
     * Lock a timesheet to prevent modifications.
     *
     * @param id timesheet identifier
     */
    @Override
    public void lock(Integer id) {
        log.info("Locking timesheet id={}", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Timesheet not found during lock: id={}", id);
                    return new NotFoundException("Timesheet not found");
                });

        t.setStatus(TimesheetStatus.LOCKED);
        log.info("Timesheet locked successfully: id={}", id);
    }

    /**
     * Retrieve all timesheets (non-paged).
     *
     * Maps TimesheetEntity -> TmsTimesheetDto using TmsTimesheetMappers.toDetail.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TmsTimesheetDto> getAll() {
        log.info("Service: fetching all timesheets");

        // fetch all entities
        var allEntities = timesheetRepo.findAll();

        log.info("Service: found {} timesheets", allEntities.size());

        var timesheet = TmsTimesheetMappers.toTimesheetResponseList(allEntities);
        return timesheet;
    }

    /**
     * Delete a timesheet by id.
     *
     * Business logic:
     * - Ensure timesheet exists; otherwise throw NotFoundException.
     * - Delete associated time entries first to avoid FK/cascade issues (safer).
     * - Delete the timesheet entity.
     */
    @Override
    public void delete(Integer id) {
        log.info("Service: deleting timesheet id={}", id);

        var ts = timesheetRepo.findById(id).orElseThrow(() -> {
            log.warn("Service: timesheet not found id={}", id);
            return new NotFoundException("Timesheet not found");
        });

        // Defensive: delete entries belonging to this timesheet explicitly (avoids FK constraint issues if cascade not configured)
        var entries = ts.getEntries();
        if (entries != null && !entries.isEmpty()) {
            log.debug("Service: deleting {} entries for timesheet id={}", entries.size(), id);

            // Use repository bulk delete for performance (deleteAll accepts a collection)
            entryRepo.deleteAll(entries);

            // Clear entries from the entity to keep persistence context consistent
            ts.getEntries().clear();
        }

        // Now delete the timesheet
        timesheetRepo.delete(ts);

        log.info("Service: timesheet deleted id={}", id);
    }

}


