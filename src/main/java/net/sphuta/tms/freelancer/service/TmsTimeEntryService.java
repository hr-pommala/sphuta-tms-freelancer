package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TimeEntryDto;

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
    void delete(Integer id);
}
