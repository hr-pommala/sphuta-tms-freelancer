package net.sphuta.tms.freelancer.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.BulkUpsertDto;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.dto.TmsTimesheetDto;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.*;
import net.sphuta.tms.freelancer.service.TmsTimesheetService;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Service implementation for Timesheets.
 *
 * Responsibilities:
 * - Create / fetch / submit / delete timesheets
 * - Bulk upsert time entries
 * - Provide weekly and monthly aggregated views (per-project)
 *
 * Notes:
 * - Weekly/monthly endpoints return per-project objects with:
 *   { projectName, timesheetID, timeentries: [{date,hours,taskId,taskName}], status }
 *
 * - The repository method findByTimesheetInAndEntryDateBetween(...) must eagerly fetch the
 *   task association (left join fetch) so taskId/taskName are available inside the transaction.
 */
@Slf4j
@Service
@Transactional
public class TmsTimesheetServiceImpl implements TmsTimesheetService {

    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    @Autowired
    private TmsTimeEntryRepository entryRepo;

    @Autowired
    private TmsProjectRepository projectRepository;

    @Autowired
    private TmsUserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;


    /**
     * Build a {@link TimeEntryEntity} from a {@link TimeEntryDto} and parent {@link TimesheetEntity}.
     *
     * - If the request includes taskId, attempt to resolve the TaskEntity and attach it.
     * - Cost is computed from rateAtEntry * hours when rate is present.
     *
     * @param t the parent TimesheetEntity
     * @param r the incoming TimeEntryDto
     * @return built (but not yet persisted) TimeEntryEntity
     */
    private TimeEntryEntity buildEntryFromReq(TimesheetEntity t, TimeEntryDto r) {
        var cost = Optional.ofNullable(r.rateAtEntry()).map(rate -> rate.multiply(r.hours())).orElse(null);

        TimeEntryEntity.TimeEntryEntityBuilder b = TimeEntryEntity.builder()
                .timesheet(t)
                .projectId(t.getProject().getId())
                .entryDate(r.entryDate())
                .description(r.description())
                .hours(r.hours())
                .rateAtEntry(r.rateAtEntry())
                .costAtEntry(cost);

        // If a taskId is provided in request, try to resolve and attach TaskEntity (lenient: if not found, leave null)
        if (r.taskId() != null) {
            var task = taskRepository.findById(r.taskId()).orElse(null);
            b.task(task);
            if (task == null) {
                log.debug("buildEntryFromReq: taskId={} not found; creating entry without task", r.taskId());
            } else {
                log.debug("buildEntryFromReq: attached taskId={} name={} to new entry", task.getId(), task.getTaskName());
            }
        }

        var entry = b.build();
        log.debug("Built TimeEntryEntity for timesheetId={} projectId={} date={} hours={} cost={}",
                t.getId(), t.getProjectId(), r.entryDate(), r.hours(), cost);
        return entry;
    }


    // -------------------------------------------------------------------------
    // Helper: total hours for a timesheet
    // -------------------------------------------------------------------------
    private BigDecimal computeTotalHours(TimesheetEntity t) {
        BigDecimal total = t.getEntries().stream()
                .map(TimeEntryEntity::getHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Computed totalHours={} for timesheetId={}", total, t.getId());
        return total;
    }

    /**
     * Build a day-by-day list spanning start..end inclusive.
     *
     * Each row contains exactly: { date (yyyy-MM-dd), hours (BigDecimal), taskId (Integer|null), taskName (String|null) }.
     *
     * This version is defensive when reading e.getTask() (catches proxy/lazy exceptions)
     * and logs the concrete entries read so you can debug why tasks may be null.
     */
    private List<Map<String, Object>> buildDailyEntryList(List<TimeEntryEntity> entries, LocalDate start, LocalDate end) {
        log.debug("buildDailyEntryList: building range {}..{} with {} DB entries", start, end, entries == null ? 0 : entries.size());

        // quick debug: log each entry and whether task is present (helps to confirm fetch behavior)
        if (entries != null && !entries.isEmpty()) {
            for (TimeEntryEntity ent : entries) {
                try {
                    var task = ent.getTask(); // may be proxy
                    log.debug("entry id={} date={} hours={} taskPresent={} taskId={}",
                            ent.getId(), ent.getEntryDate(), ent.getHours(),
                            task != null, task != null ? task.getId() : null);
                } catch (RuntimeException ex) {
                    // safe log if proxy cannot be initialized here
                    log.debug("entry id={} date={} hours={} task: cannot access (proxy) -> {}", ent.getId(), ent.getEntryDate(), ent.getHours(), ex.toString());
                }
            }
        }

        // group by date
        Map<LocalDate, List<TimeEntryEntity>> byDate = new TreeMap<>();
        for (TimeEntryEntity e : Optional.ofNullable(entries).orElse(List.of())) {
            // guard if entryDate is null
            if (e.getEntryDate() == null) {
                log.debug("buildDailyEntryList: skipping entry id={} with null entryDate", e.getId());
                continue;
            }
            byDate.computeIfAbsent(e.getEntryDate(), d -> new ArrayList<>()).add(e);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            List<TimeEntryEntity> dayEntries = byDate.getOrDefault(cursor, List.of());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", cursor.toString());

            if (dayEntries.isEmpty()) {
                row.put("hours", BigDecimal.ZERO);
                row.put("taskId", null);
                row.put("taskName", null);
                log.trace("buildDailyEntryList: date={} -> no entries (zero-row)", cursor);
            } else {
                BigDecimal total = BigDecimal.ZERO;
                Integer chosenTaskId = null;
                String chosenTaskName = null;
                boolean multipleTasks = false;

                for (TimeEntryEntity e : dayEntries) {
                    total = total.add(Optional.ofNullable(e.getHours()).orElse(BigDecimal.ZERO));

                    // Read task defensively: some JPA providers may throw when accessing a lazy proxy
                    Integer tid = null;
                    String tname = null;
                    try {
                        var t = e.getTask();
                        if (t != null) {
                            tid = t.getId();
                            tname = t.getTaskName();
                        }
                    } catch (RuntimeException ex) {
                        // Could not access the task (proxy or other issue) — treat as no task
                        log.debug("buildDailyEntryList: unable to access task for entryId={} -> {}", e.getId(), ex.toString());
                    }

                    if (tid != null) {
                        if (chosenTaskId == null) {
                            chosenTaskId = tid;
                            chosenTaskName = tname;
                        } else if (!Objects.equals(chosenTaskId, tid)) {
                            multipleTasks = true;
                        }
                    } else {
                        // entry without task while another entry had a task -> ambiguous
                        if (chosenTaskId != null) {
                            multipleTasks = true;
                        }
                    }
                }

                row.put("hours", total);
                if (multipleTasks) {
                    row.put("taskId", null);
                    row.put("taskName", null);
                    log.trace("buildDailyEntryList: date={} -> multiple/ambiguous tasks; hours={}", cursor, total);
                } else {
                    row.put("taskId", chosenTaskId);
                    row.put("taskName", chosenTaskName);
                    log.trace("buildDailyEntryList: date={} -> chosenTaskId={} hours={}", cursor, chosenTaskId, total);
                }
            }

            rows.add(row);
            cursor = cursor.plusDays(1);
        }

        log.debug("buildDailyEntryList: built {} rows for range {}..{}", rows.size(), start, end);
        return rows;
    }

    // -------------------------------------------------------------------------
    // Standard CRUD / bulk methods (unchanged behaviour)
    // -------------------------------------------------------------------------
    @Override
    @Operation(summary = "Create timesheet", description = "Create a new timesheet for the given project and period.")
    public TmsTimesheetDto create(TmsTimesheetDto req) {
        log.info("Request received to create timesheet: projectId={}, periodStart={}, periodEnd={}",
                req.projectId(), req.periodStart(), req.periodEnd());

        if (req.periodEnd().isBefore(req.periodStart())) {
            log.error("Validation failed: periodEnd={} is before periodStart={}", req.periodEnd(), req.periodStart());
            throw new ValidationException("periodEnd must be >= periodStart");
        }

        if (Optional.ofNullable(req.projectId()).filter(projectRepository::existsById).isEmpty()) {
            log.error("Project not found: projectId={}", req.projectId());
            throw new NotFoundException(TmsMessages.PROJECT_NOT_FOUND);
        }

        timesheetRepo.findByProjectIdAndPeriodStartAndPeriodEnd(req.projectId(), req.periodStart(), req.periodEnd())
                .ifPresent(t -> {
                    log.error("Conflict: Timesheet already exists for projectId={} period {}..{}",
                            req.projectId(), req.periodStart(), req.periodEnd());
                    throw new ConflictException(TmsMessages.TIMESHEET_CONFLICT);
                });
        var project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));

        TimesheetEntity t = TimesheetEntity.builder()
                .project(project)
                .periodStart(req.periodStart())
                .periodEnd(req.periodEnd())
                .status(TimesheetStatus.DRAFT)
                .build();

        timesheetRepo.save(t);
        log.info("Created new timesheet id={} projectId={} {}..{}", t.getId(), t.getProjectId(),
                t.getPeriodStart(), t.getPeriodEnd());

        return TmsTimesheetMappers.toDetail(t);
    }

    @Override
    @Transactional(readOnly = true)
    public TmsTimesheetDto get(int id) {
        log.info("Fetching timesheet by id={}", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Timesheet not found: id={}", id);
                    return new NotFoundException(TmsMessages.TIMESHEET_NOT_FOUND);
                });

        // force load entries
        t.getEntries().size();
        return TmsTimesheetMappers.toDetail(t);
    }

    @Override
    public TmsTimesheetDto submit(int id) {
        log.info("Submitting timesheet id={} for approval", id);

        var t = timesheetRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Timesheet not found during submit: id={}", id);
                    return new NotFoundException(TmsMessages.TIMESHEET_NOT_FOUND);
                });

        t.setStatus(TimesheetStatus.APPROVED);
        log.info("Timesheet submitted: id={} status={}", id, t.getStatus());

        return TmsTimesheetMappers.toDetail(t);
    }

    @Override
    @Operation(summary = "Bulk upsert entries", description = "Insert or update multiple entries in a timesheet.")
    public BulkUpsertDto bulkUpsert(int timesheetId, BulkUpsertDto req) {
        log.info("Bulk upsert requested: timesheetId={}, rows={}",
                timesheetId, (req.entries() == null ? 0 : req.entries().size()));

        if (Optional.ofNullable(req.entries()).filter(entries -> !entries.isEmpty()).isEmpty()) {
            log.warn("No entries provided for bulk upsert");
            return TmsTimesheetMappers.toBulkUpsertResponse(List.of(), req.mode(), 0, 0, 0, BigDecimal.ZERO);
        }

        var t = timesheetRepo.findById(timesheetId)
                .orElseThrow(() -> {
                    log.error("Timesheet not found: id={}", timesheetId);
                    return new NotFoundException(TmsMessages.TIMESHEET_NOT_FOUND);
                });

        if (!t.getStatus().isMutable()) {
            log.error("Timesheet is LOCKED and cannot be modified: id={}", timesheetId);
            throw new ConflictException(TmsMessages.TIMESHEET_LOCKED);
        }

        int inserted = 0, updated = 0;
        List<TimeEntryDto> processed = new ArrayList<>();

        for (var r : req.entries()) {
            log.debug("Processing entry: date={}, desc='{}', hours={}, rate={}, taskId={}",
                    r.entryDate(), r.description(), r.hours(), r.rateAtEntry(), r.taskId());

            // fetch project start date
            LocalDate projectStartDate = projectRepository.findById(t.getProjectId())
                    .map(ProjectEntity::getStartDate)
                    .orElseThrow(() -> new NotFoundException(TmsMessages.PROJECT_NOT_FOUND));

            // updated validation logic
            if (r.entryDate().isBefore(projectStartDate) || r.entryDate().isAfter(t.getPeriodEnd())) {
                log.warn("Validation failed: entry outside project range. entry={}, projectStart={} periodEnd={}",
                        r.entryDate(), projectStartDate, t.getPeriodEnd());
                throw new ValidationException("Entry date outside project active range");
            }
            // validate hours > 0
            if (Optional.ofNullable(r.hours()).filter(h -> h.compareTo(BigDecimal.ZERO) > 0).isEmpty()) {
                log.warn("Validation failed: entry with invalid hours. entry={}", r);
                throw new ValidationException(TmsMessages.HOURS_INVALID);
            }

            // check existing by timesheet + date + description
            var existing = entryRepo.findByTimesheetAndEntryDateAndDescription(t, r.entryDate(), r.description()).orElse(null);

            if (existing == null) {
                // Insert path: build entity (buildEntryFromReq will attach task if taskId present)
                var e = buildEntryFromReq(t, r);
                entryRepo.save(e);
                // keep in-memory timesheet entries synced
                t.getEntries().add(e);
                inserted++;

                // Map saved entity to DTO (mapper will read task safely)
                processed.add(TmsTimesheetMappers.toTimeEntryDto(e));
                log.info("Inserted new entry: id={} date={} desc='{}' hours={} rate={} taskId={}",
                        e.getId(), e.getEntryDate(), e.getDescription(), e.getHours(), e.getRateAtEntry(),
                        e.getTask() != null ? e.getTask().getId() : null);
            } else {
                // Update path: update fields
                existing.setHours(r.hours());
                existing.setRateAtEntry(r.rateAtEntry());
                existing.setCostAtEntry(Optional.ofNullable(r.rateAtEntry()).map(rate -> rate.multiply(r.hours())).orElse(null));
                // ensure projectId remains in sync (defensive)
                existing.setProjectId(t.getProjectId());

                // If request contains taskId (non-null), resolve and set the task (this allows changing task)
                if (r.taskId() != null) {
                    var task = taskRepository.findById(r.taskId()).orElse(null);
                    existing.setTask(task); // if task==null this will clear the association
                    if (task == null) {
                        log.debug("bulkUpsert: update - taskId={} not found; entry id={} will have task=null", r.taskId(), existing.getId());
                    } else {
                        log.debug("bulkUpsert: update - attached taskId={} to entry id={}", task.getId(), existing.getId());
                    }
                }
                // explicitly save updated entity so changes are flushed/persisted
                entryRepo.save(existing);

                updated++;
                processed.add(TmsTimesheetMappers.toTimeEntryDto(existing));
                log.info("Updated existing entry: id={} date={} desc='{}' newHours={} newRate={} taskId={}",
                        existing.getId(), r.entryDate(), r.description(), r.hours(), r.rateAtEntry(),
                        existing.getTask() != null ? existing.getTask().getId() : null);
            }
        }

        BigDecimal total = computeTotalHours(t);
        var response = TmsTimesheetMappers.toBulkUpsertResponse(processed, req.mode(), inserted, updated, 0, total);
        log.info("Bulk upsert completed: timesheetId={} inserted={} updated={} totalHours={}", timesheetId, inserted, updated, total);

        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public List<TmsTimesheetDto> getAll() {
        log.info("Service: fetching all timesheets");
        var allEntities = timesheetRepo.findAll();
        return TmsTimesheetMappers.toTimesheetResponseList(allEntities);
    }

    @Override
    public void delete(int id) {
        log.info("Service: deleting timesheet id={}", id);

        var ts = timesheetRepo.findById(id).orElseThrow(() -> {
            log.warn("Service: timesheet not found id={}", id);
            return new NotFoundException(TmsMessages.TIMESHEET_NOT_FOUND);
        });

        var entries = ts.getEntries();
        Optional.ofNullable(entries)
                .filter(e -> !e.isEmpty())
                .ifPresent(e -> {
                    entryRepo.deleteAll(e);
                    ts.getEntries().clear();
                });

        timesheetRepo.delete(ts);
        log.info("Service: timesheet deleted id={}", id);
    }

    // -------------------------------------------------------------------------
    // Weekly / Monthly retrievals (RETURN FORMAT CHANGED)
    // -------------------------------------------------------------------------

    /**
     * Get weekly time entries for a user, aggregated per project.
     * The week is defined as Monday to Sunday containing today's date.
     * The returned map is keyed by projectId and contains:
     **/
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Map<String, Object>> getWeeklyTimeEntries(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found: " + userEmail));

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        log.info("getWeeklyTimeEntries email={} week {}..{}", userEmail, weekStart, weekEnd);
        return getTimeEntriesForUserForRange(user.getEmail(), weekStart, weekEnd);
    }

    /**
     * Get monthly time entries for a user, aggregated per project.
     * The month is defined as the current calendar month.
     * The returned map is keyed by projectId and contains:
     **/
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Map<String, Object>> getMonthlyTimeEntries(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found: " + userEmail));

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        log.info("getMonthlyTimeEntries email={} month {}..{}", userEmail, monthStart, monthEnd);
        return getTimeEntriesForUserForRange(user.getEmail(), monthStart, monthEnd);
    }

    /**
     * Helper: aggregate time entries across projects for a user in a date range.
     *
     * The returned map keyed by projectId contains:
     * - projectName (String)
     * - timesheetID (Integer | null)  // latest timesheet overlapping range
     * - status (String)                // timesheet status or "NONE"
     * - timeentries (List of {date,hours,taskId,taskName})  // one row per calendar day in range
     *
     * @param userEmail user's email
     * @param rangeStart inclusive start
     * @param rangeEnd inclusive end
     * @return map keyed by projectId
     */
    @Transactional(readOnly = true)
    private Map<Integer, Map<String, Object>> getTimeEntriesForUserForRange(String userEmail,
                                                                            LocalDate rangeStart,
                                                                            LocalDate rangeEnd) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(TmsMessages.USER_NOT_FOUND + userEmail));

        log.debug("Building project entries for email={} range {}..{}", userEmail, rangeStart, rangeEnd);

        List<ProjectEntity> projects = projectRepository.findByUserId(user.getId());
        Map<Integer, Map<String, Object>> result = new LinkedHashMap<>();

        for (ProjectEntity p : projects) {
            int projectId = p.getId();
            Map<String, Object> projectMap = new LinkedHashMap<>();
            projectMap.put("projectName", p.getName());
            projectMap.put("status", "NONE");
            projectMap.put("timesheetID", null);

            // find overlapping timesheets for the requested range
            List<TimesheetEntity> timesheets = timesheetRepo.findByProjectIdAndPeriodOverlapping(projectId, rangeStart, rangeEnd);

            // Attempt to get the latest timesheet (if any) to supply status/timesheetID
            TimesheetEntity latest = null;
            if (!timesheets.isEmpty()) {
                latest = timesheets.stream()
                        .max(Comparator.comparing(TimesheetEntity::getPeriodStart))
                        .orElse(timesheets.get(0));
                projectMap.put("status", latest.getStatus() != null ? latest.getStatus().name() : "UNKNOWN");
                projectMap.put("timesheetID", latest.getId());
            }

            // Fetch entries (repo uses left join fetch to eagerly load task)
            List<TimeEntryEntity> entries = entryRepo.findByTimesheetInAndEntryDateBetween(timesheets, rangeStart, rangeEnd);

            // Build day-by-day rows (one per calendar day in range) with task info when available
            List<Map<String, Object>> entryRows = buildDailyEntryList(entries, rangeStart, rangeEnd);

            projectMap.put("timeentries", entryRows);
            log.debug("Project id={} -> mapped {} day-rows (status={}, timesheetID={})",
                    projectId, entryRows.size(), projectMap.get("status"), projectMap.get("timesheetID"));

            result.put(projectId, projectMap);
        }

        log.info("getTimeEntriesForUserForRange: completed for user={} projectsCount={}", userEmail, result.size());
        return result;
    }

    // -------------------------------------------------------------------------
    // Per-project range retrieval (unchanged behaviour)
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTimeEntriesByProject(int projectId, LocalDate start, LocalDate end) {
        log.info("getTimeEntriesByProject projectId={} range {}..{}", projectId, start, end);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must be >= start");
        }

        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException(TmsMessages.PROJECT_NOT_FOUND + projectId);
        }

        List<TimesheetEntity> timesheets = timesheetRepo.findByProjectIdAndPeriodOverlapping(projectId, start, end);
        log.debug("Found {} timesheets for project {}", timesheets.size(), projectId);

        Map<String, Object> projectMap = new LinkedHashMap<>();
        projectRepository.findById(projectId).ifPresent(p -> projectMap.put("projectName", p.getName()));
        projectMap.put("status", "NONE");
        projectMap.put("timeentries", new LinkedHashMap<String, BigDecimal>());

        if (!timesheets.isEmpty()) {
            TimesheetEntity latest = timesheets.stream()
                    .max(Comparator.comparing(TimesheetEntity::getPeriodStart))
                    .orElse(timesheets.get(0));
            projectMap.put("status", latest.getStatus() != null ? latest.getStatus().name() : "UNKNOWN");

            List<TimeEntryEntity> entries = entryRepo.findByTimesheetInAndEntryDateBetween(timesheets, start, end);

            LinkedHashMap<String, BigDecimal> dayMap = new LinkedHashMap<>();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                dayMap.put(d.toString(), BigDecimal.ZERO);
            }
            for (TimeEntryEntity e : entries) {
                String day = e.getEntryDate().toString();
                dayMap.put(day, dayMap.getOrDefault(day, BigDecimal.ZERO)
                        .add(Optional.ofNullable(e.getHours()).orElse(BigDecimal.ZERO)));
            }
            projectMap.put("timeentries", dayMap);
        }

        return projectMap;
    }
}
