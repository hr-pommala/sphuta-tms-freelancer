package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link TimeEntryEntity}.
 *
 * <p>This interface extends {@link JpaRepository} to inherit
 * CRUD operations for time entry entities and defines custom
 * query methods for retrieving entries by timesheet, date, and description.</p>
 *
 * <p>It acts as the Data Access Layer, separating persistence logic
 * from business logic.</p>
 */
public interface TmsTimeEntryRepository extends JpaRepository<TimeEntryEntity, Integer> {

    /**
     * Retrieves a list of time entries associated with the given timesheet.
     *
     * @param timesheet the timesheet entity used as a filter
     * @return list of {@link TimeEntryEntity} belonging to the specified timesheet
     */
    List<TimeEntryEntity> findByTimesheet(TimesheetEntity timesheet);

    /**
     * Retrieves a time entry by timesheet, entry date, and description.
     *
     * <p>This ensures uniqueness of a time entry within a timesheet by date and description.</p>
     *
     * @param t the timesheet entity
     * @param d the entry date
     * @param desc the description of the time entry
     * @return an {@link Optional} containing the matching time entry if found, or empty if not
     */
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndDescription(TimesheetEntity t, LocalDate d, String desc);
}
