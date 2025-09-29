package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TaskEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link TimeEntryEntity} persistence operations.
 *
 * <p>This interface extends {@link JpaRepository} to provide standard CRUD operations,
 * plus a small set of domain-specific finder methods used by the timesheet service.</p>
 *
 * <p>Important: the custom query {@link #findByTimesheetInAndEntryDateBetween(List, LocalDate, LocalDate)}
 * uses {@code left join fetch e.task} so that the associated {@code TaskEntity} is eagerly
 * fetched with each TimeEntry. This avoids lazy-loading issues when mapping entities
 * to JSON outside the persistence provider's session.</p>
 */
public interface TmsTimeEntryRepository extends JpaRepository<TimeEntryEntity, Integer> {

    /**
     * Return all time entries that belong to the given timesheet.
     *
     * @param timesheet the timesheet entity whose entries should be returned
     * @return list of time entry entities (possibly empty)
     */
    List<TimeEntryEntity> findByTimesheet(TimesheetEntity timesheet);

    /**
     * Find a time entry by timesheet + date + description.
     *
     * <p>Used by bulk upsert to detect duplicate entries (same date + description)</p>
     *
     * @param timesheet  timesheet to search within
     * @param entryDate  date of the entry
     * @param description textual description of the entry
     * @return optional time entry (present when a match is found)
     */
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndDescription(
            TimesheetEntity timesheet,
            LocalDate entryDate,
            String description
    );

    /**
     * Fetch time entries that belong to any of the provided timesheets and whose
     * entryDate is within the inclusive range [start, end].
     *
     * <p>Notes:
     * - The query uses {@code left join fetch e.task} to eagerly load the task association.
     * - Results are ordered by entry date then id to make responses deterministic.</p>
     *
     * @param timesheets list of timesheets to include in the search
     * @param start      inclusive start date
     * @param end        inclusive end date
     * @return list of matching {@link TimeEntryEntity} with task association fetched
     */
    @Query("""
        select e from TimeEntryEntity e
        left join fetch e.task t
        where e.timesheet in :timesheets
        and e.entryDate between :start and :end
        order by e.entryDate, e.id
    """)
    List<TimeEntryEntity> findByTimesheetInAndEntryDateBetween(
            @Param("timesheets") List<TimesheetEntity> timesheets,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    /**
     * Derived query that matches by timesheet + entryDate + task entity.
     * Useful when you have a managed TaskEntity instance.


     */
    @Query("select e from TimeEntryEntity e where e.timesheet = :timesheet and e.entryDate = :entryDate and e.task = :task")
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndTask(@Param("timesheet") TimesheetEntity timesheet,
                                                                 @Param("entryDate") LocalDate entryDate,
                                                                 @Param("task") TaskEntity task);

    /**
     * Derived query that matches by timesheet + entryDate + task id.
     * Use this when you only have a taskId (no TaskEntity instance).
*/
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndTask_Id(
            TimesheetEntity timesheet,
            LocalDate entryDate,
            Integer taskId
    );
    // Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndTask_Id(TimesheetEntity timesheet, LocalDate entryDate, Integer taskId);
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndDescriptionIgnoreCase(TimesheetEntity timesheet, LocalDate entryDate, String description);
}
