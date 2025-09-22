package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing Time Entries in the Freelancer Timesheet Management System.
 * <p>
 * Provides business operations such as creating and deleting time entries.
 * </p>
 */
public interface TmsTimeEntryService {

    /**
     * Creates a new Time Entry.
     *
     * @param req {@link TimeEntryDto} containing the details required
     *            to create a new time entry (e.g., timesheet ID, entry date, description, hours, etc.)
     * @return {@link TimeEntryDto} containing the details of the created time entry
     */
    TimeEntryDto create(TimeEntryDto req);

    /**
     * Deletes an existing Time Entry by its ID.
     *
     * @param id the unique identifier of the time entry to delete
     */
    void delete(int id);

    /**
     * Retrieve all time entries across all timesheets.
     *
     * @return list of {@link TimeEntryDto} (may be empty)
     */
    List<TimeEntryDto> getAll();

    /**
     * Find all entries belonging to any of the provided timesheets and
     * with entryDate between start and end (inclusive).
     *
     * Spring Data JPA derived query — will be implemented automatically.
     */
}
