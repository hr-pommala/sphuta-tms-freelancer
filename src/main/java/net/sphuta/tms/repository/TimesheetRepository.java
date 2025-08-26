package net.sphuta.tms.repository;

import net.sphuta.tms.enam.TimesheetStatus;
import net.sphuta.tms.entity.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link Timesheet} entity.
 * <p>
 * This extends {@link JpaRepository} which provides basic CRUD operations.
 * Additionally, custom query methods are declared here for timesheet-specific use cases.
 */
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    /**
     * Find a timesheet by project and exact period (start and end).
     *
     * @param projectId ID of the project
     * @param ps        period start date
     * @param pe        period end date
     * @return an Optional containing the timesheet if found
     */
    Optional<Timesheet> findByProjectIdAndPeriodStartAndPeriodEnd(UUID projectId, LocalDate ps, LocalDate pe);

    /**
     * Find timesheets by project and status within a date range.
     *
     * @param projectId ID of the project
     * @param status    status filter (e.g., DRAFT, APPROVED, LOCKED)
     * @param from      minimum start date (inclusive)
     * @param to        maximum end date (inclusive)
     * @param pageable  pagination information
     * @return a Page of timesheets matching criteria
     */
    Page<Timesheet> findByProjectIdAndStatusAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
            UUID projectId, TimesheetStatus status, LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Find all timesheets belonging to a specific project.
     *
     * @param projectId ID of the project
     * @param pageable  pagination information
     * @return a Page of timesheets for the given project
     */
    Page<Timesheet> findByProjectId(UUID projectId, Pageable pageable);

    /**
     * Find all timesheets by status across all projects.
     *
     * @param status   status filter
     * @param pageable pagination information
     * @return a Page of timesheets with the given status
     */
    Page<Timesheet> findByStatus(TimesheetStatus status, Pageable pageable);
}
