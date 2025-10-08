package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link TimesheetEntity}.
 *
 * <p>Extends {@link JpaRepository} to provide built-in CRUD functionality
 * and also defines custom query methods for searching timesheets
 * by project, status, and date ranges.</p>
 *
 * <p>This acts as the Data Access Layer for timesheet entities,
 * separating database interactions from business logic.</p>
 */
public interface TmsTimesheetRepository extends JpaRepository<TimesheetEntity, Integer> {

    /**
     * Finds a timesheet for a given project within a specific period range.
     *
     * <p>This method ensures a timesheet is uniquely identified
     * by projectId, periodStart, and periodEnd.</p>
     *
     * @param projectId ID of the project
     * @param ps start date of the period
     * @param pe end date of the period
     * @return an {@link Optional} containing the timesheet if found
     */
    Optional<TimesheetEntity> findByProjectIdAndPeriodStartAndPeriodEnd(int projectId, LocalDate ps, LocalDate pe);


    /**
     * Finds all timesheets for a given project.
     *
     * <p>Supports pagination to avoid fetching large datasets at once.</p>
     *
     * @param projectId ID of the project
     * @param pageable pagination details
     * @return a {@link Page} of timesheets belonging to the project
     */
    Page<TimesheetEntity> findByProjectId(int projectId, Pageable pageable);

    /**
     * Finds all timesheets by their status.
     *
     * <p>Useful for retrieving timesheets across projects
     * that share the same workflow state.</p>
     *
     * @param status status of the timesheet
     * @param pageable pagination details
     * @return a {@link Page} of timesheets matching the status
     */
    Page<TimesheetEntity> findByStatus(TimesheetStatus status, Pageable pageable);

/**
        * Find timesheets for a project that overlap the given range [start, end].
            * Overlap condition: timesheet.periodStart <= end AND timesheet.periodEnd >= start
     */
    @Query("SELECT t FROM TimesheetEntity t WHERE t.projectId = :projectId AND t.periodStart <= :end AND t.periodEnd >= :start")
    List<TimesheetEntity> findByProjectIdAndPeriodOverlapping( int projectId, LocalDate start, LocalDate end);
}

