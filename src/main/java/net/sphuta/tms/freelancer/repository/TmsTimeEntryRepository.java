package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ==========================================================
 * TmsTimeEntryRepository
 * ==========================================================
 *
 * Spring Data JPA repository for {@link TimeEntryEntity}.
 *
 * Responsibilities:
 * - Provides CRUD operations for time entries via {@link JpaRepository}.
 * - Declares custom queries for business-specific use cases.
 *
 * Typical usage:
 * - Fetching uninvoiced, approved time entries for invoice generation.
 * - Checking whether a client has related time entries before deletion.
 */
public interface TmsTimeEntryRepository extends JpaRepository<TimeEntryEntity, Integer> {


    List<TimeEntryEntity> findByTimesheet(TimesheetEntity timesheet);
    /**
     * Checks whether the given client has at least one time entry.
     *
     * <p>This ensures uniqueness of a time entry within a timesheet by date and description.</p>
     *
     * @param t the timesheet entity
     * @param d the entry date
     * @param desc the description of the time entry
     * @return an {@link Optional} containing the matching time entry if found, or empty if not* @return true if at least one time entry exists for the client; false otherwise
     */
    Optional<TimeEntryEntity> findByTimesheetAndEntryDateAndDescription(TimesheetEntity t, LocalDate d, String desc);

}
