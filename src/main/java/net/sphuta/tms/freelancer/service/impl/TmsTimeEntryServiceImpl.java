package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.entity.TaskEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.exception.ApiExceptions;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.repository.TaskRepository;
import net.sphuta.tms.freelancer.service.TmsTimeEntryService;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import java.math.BigDecimal;
import java.time.OffsetTime;
import java.util.List;
import java.util.Optional;

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

    /** Repository for tasks — used to resolve taskId -> TaskEntity when creating entries. */
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TmsProjectRepository projectRepository;
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
     * <p>The cost is computed as <code>hours × rateAtEntry</code>. Start and end times
     * are auto-generated based on current time and hours worked.
     *
     * <p>If {@code req.taskId()} is provided, we attempt to resolve the {@link TaskEntity}
     * and attach it to the saved entry. We are lenient: if the task id does not exist we
     * log and continue (you can change to strict validation if desired).
     *
     * @param req the incoming {@link TimeEntryDto} request
     * @return a response DTO representing the created time entry
     * @throws NotFoundException    if the timesheet does not exist
     * @throws ConflictException    if the timesheet is locked
     * @throws ApiExceptions.ValidationException if hours or entry date are invalid
     */
    @Override
    public TimeEntryDto create(TimeEntryDto req) {
        log.info("create time-entry: timesheetId={}, entryDate={}, hours={}, rateAtEntry={}, taskId={}",
                req.timesheetId(), req.entryDate(), req.hours(), req.rateAtEntry(), req.taskId());

        // Check existence of the timesheet
        TimesheetEntity t = timesheetRepo.findById(req.timesheetId())
                .orElseThrow(() -> {
                    log.warn("create time-entry: timesheet not found: {}", req.timesheetId());
                    return new NotFoundException("Timesheet not found");
                });

        // Ensure timesheet is mutable
        if (!t.getStatus().isMutable()) {
            log.warn("create time-entry: timesheet={} status={} locked", t.getId(), t.getStatus());
            throw new ConflictException("Timesheet is LOCKED and cannot be modified");
        }

        // Validate entry date is within timesheet period
        if (req.entryDate().isBefore(t.getPeriodStart()) || req.entryDate().isAfter(t.getPeriodEnd())) {
            log.warn("create time-entry: entryDate {} outside [{}, {}] for timesheet={}",
                    req.entryDate(), t.getPeriodStart(), t.getPeriodEnd(), t.getId());
            throw new ApiExceptions.ValidationException("entryDate outside timesheet period");
        }

        // Validate hours > 0
        if (Optional.ofNullable(req.hours()).filter(h -> h.compareTo(BigDecimal.ZERO) > 0).isEmpty()) {
            log.warn("create time-entry: invalid hours={} for timesheet={}", req.hours(), t.getId());
            throw new ApiExceptions.ValidationException("hours must be > 0");
        }

        // Compute cost if rate present
        var cost = Optional.ofNullable(req.rateAtEntry())
                .map(rate -> rate.multiply(req.hours()))
                .orElse(null);
        log.debug("create time-entry: computed cost={} (hours={} x rate={})", cost, req.hours(), req.rateAtEntry());

        // Compute start/end times
        OffsetTime startTime = OffsetTime.now();
        long minutes = req.hours().multiply(BigDecimal.valueOf(60)).longValue();
        OffsetTime endTime = startTime.plusMinutes(minutes);

        // Resolve task if provided
        TaskEntity task = null;
        if (req.taskId() != null) {
            task = taskRepository.findById(req.taskId()).orElse(null);
            if (task == null) {
                log.debug("create time-entry: taskId={} not found; creating entry without task", req.taskId());
            } else {
                log.debug("create time-entry: resolved taskId={} name={}", task.getId(), task.getTaskName());
            }
        }

        // Build entity using builder so we can conditionally set task
        TimeEntryEntity.TimeEntryEntityBuilder b = TimeEntryEntity.builder()
                .timesheet(t)
                .entryDate(req.entryDate())
                .projectId(t.getProjectId())
                .description(req.description())
                .hours(req.hours())
                .rateAtEntry(req.rateAtEntry())
                .costAtEntry(cost)
                .startTime(startTime)
                .endTime(endTime);

        if (task != null) {
            b.task(task);
        }

        TimeEntryEntity e = b.build();

        // Persist and link to timesheet
        entryRepo.save(e);
        // ensure timesheet entries list reflects saved entry in-memory
        t.getEntries().add(e);

        log.info("create time-entry: success entryId={} timesheetId={} start={} end={} taskId={}",
                e.getId(), t.getId(), e.getStartTime(), e.getEndTime(), e.getTask() != null ? e.getTask().getId() : null);

        // Map to response DTO (mapper will read e.getTask() safely)
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
     * @throws NotFoundException  if entry does not exist
     * @throws ConflictException if entry is already invoiced
     */
    @Override
    public void delete(int id) {
        log.info("delete time-entry: entryId={}", id);

        var e = entryRepo.findById(id).orElseThrow(() -> {
            log.warn("delete time-entry: not found entryId={}", id);
            return new NotFoundException("Time entry not found");
        });

        if (Optional.ofNullable(e.getDescription())
                .map(String::toLowerCase)
                .filter(desc -> desc.contains("[invoiced]"))
                .isPresent()) {
            log.warn("delete time-entry: invoiced entryId={}", id);
            throw new ConflictException("Time entry already invoiced; cannot delete");
        }

        entryRepo.delete(e);
        log.info("delete time-entry: success entryId={}", id);
    }

    /**
     * Retrieve all time entries across all timesheets.
     */
    @Override
    public List<TimeEntryDto> getAll() {
        log.info("Service: fetching all time-entries");
        var all = entryRepo.findAll();
        log.info("Service: found {} time-entries", all.size());
        return TmsTimesheetMappers.toEntryResponseList(all);
    }

}
