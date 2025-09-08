package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.exception.ApiExceptions;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.service.TmsTimeEntryService;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetTime;
import java.util.List;

/**
 * Implementation of {@link TmsTimeEntryService}.
 *
 * <p>This service provides business logic for managing {@link TimeEntryEntity}
 * within a {@link TimesheetEntity}. It handles creation and deletion of time entries
 * with validations such as timesheet existence, date range checks, mutability,
 * and invoicing restrictions.
 *
 * <p>Transaction boundaries are managed at the class level via {@link Transactional}.
 */
@Slf4j
@Service
@Transactional
public class TmsTimeEntryServiceImpl implements TmsTimeEntryService {

    /** Repository for managing time-entry persistence. */
    @Autowired
    private TmsTimeEntryRepository entryRepo;

    /** Repository for managing timesheet persistence. */
    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    /**
     * Creates a new {@link TimeEntryEntity} under an existing {@link TimesheetEntity}.
     *
     * <p>Validations performed:
     * <ul>
     *   <li>Timesheet existence check</li>
     *   <li>Timesheet mutability (not locked)</li>
     *   <li>Entry date within timesheet period</li>
     *   <li>Hours must be greater than zero</li>
     * </ul>
     *
     * <p>The cost is computed as <code>hours × rateAtEntry. Start and end times
     * are auto-generated based on current time and hours worked.
     *
     * @param req the incoming {@link TimeEntryDto} request
     * @return a response DTO representing the created time entry
     * @throws ApiExceptions.NotFoundException    if the timesheet does not exist
     * @throws ApiExceptions.ConflictException   if the timesheet is locked
     * @throws ApiExceptions.ValidationException if hours or entry date are invalid
     */
    @Override
    public TimeEntryDto create(TimeEntryDto req) {
        log.info("create time-entry: timesheetId={}, entryDate={}, hours={}, rateAtEntry={}",
                req.timesheetId(), req.entryDate(), req.hours(), req.rateAtEntry());

        // ✅ Check if the timesheet exists
        TimesheetEntity t = timesheetRepo.findById(req.timesheetId())
                .orElseThrow(() -> {
                    log.warn("create time-entry: timesheet not found: {}", req.timesheetId());
                    return new NotFoundException("Timesheet not found");
                });

        // ✅ Ensure the timesheet is mutable (not locked)
        if (!t.getStatus().isMutable()) {
            log.warn("create time-entry: timesheet={} status={} locked", t.getId(), t.getStatus());
            throw new ConflictException("Timesheet is LOCKED and cannot be modified");
        }

        // ✅ Ensure the entry date falls within the timesheet’s valid period
        if (req.entryDate().isBefore(t.getPeriodStart()) || req.entryDate().isAfter(t.getPeriodEnd())) {
            log.warn("create time-entry: entryDate {} outside [{}, {}] for timesheet={}",
                    req.entryDate(), t.getPeriodStart(), t.getPeriodEnd(), t.getId());
            throw new ApiExceptions.ValidationException("entryDate outside timesheet period");
        }

        // ✅ Validate hours > 0
        if (req.hours() == null || req.hours().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("create time-entry: invalid hours={} for timesheet={}", req.hours(), t.getId());
            throw new ApiExceptions.ValidationException("hours must be > 0");
        }

        // ✅ Calculate cost (if rate is provided)
        var cost = req.rateAtEntry() == null ? null : req.rateAtEntry().multiply(req.hours());
        log.debug("create time-entry: cost computed={} (hours={} x rate={})", cost, req.hours(), req.rateAtEntry());

        // ✅ Generate start and end times based on current timestamp and hours worked
        OffsetTime startTime = OffsetTime.now();
        long minutes = req.hours().multiply(BigDecimal.valueOf(60)).longValue();
        OffsetTime endTime = startTime.plusMinutes(minutes);

        // ✅ Build new TimeEntry entity
        var e = TimeEntryEntity.builder()
                .timesheet(t)
                .entryDate(req.entryDate())
                .description(req.description())
                .hours(req.hours())
                .rateAtEntry(req.rateAtEntry())
                .costAtEntry(cost)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        // ✅ Persist entry and link to timesheet
        entryRepo.save(e);
        t.getEntries().add(e);

        log.info("create time-entry: success entryId={} timesheetId={} start={} end={}",
                e.getId(), t.getId(), e.getStartTime(), e.getEndTime());

        return TmsTimesheetMappers.toEntryResponse(e);
    }

    /**
     * Deletes a {@link TimeEntryEntity} by ID.
     *
     * <p>Validations performed:
     * <ul>
     *   <li>Entry existence check</li>
     *   <li>Invoicing check (cannot delete invoiced entries)</li>
     * </ul>
     *
     * @param id the ID of the entry to delete
     * @throws ApiExceptions.NotFoundException  if entry does not exist
     * @throws ApiExceptions.ConflictException if entry is already invoiced
     */
    @Override
    public void delete(Integer id) {
        log.info("delete time-entry: entryId={}", id);

        // ✅ Lookup entry by ID
        var e = entryRepo.findById(id).orElseThrow(() -> {
            log.warn("delete time-entry: not found entryId={}", id);
            return new NotFoundException("Time entry not found");
        });

        // ✅ Prevent deletion of invoiced entries
        if (e.getDescription() != null && e.getDescription().toLowerCase().contains("[invoiced]")) {
            log.warn("delete time-entry: invoiced entryId={}", id);
            throw new ConflictException("Time entry already invoiced; cannot delete");
        }

        // ✅ Perform deletion
        entryRepo.delete(e);
        log.info("delete time-entry: success entryId={}", id);
    }

    /**
     * Retrieve all time entries across all timesheets.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getAll() {
        log.info("Service: fetching all time-entries");

        var all = entryRepo.findAll();

        log.info("Service: found {} time-entries", all.size());

        var entryList = TmsTimesheetMappers.toEntryResponseList(all);
        return entryList;
    }

}
