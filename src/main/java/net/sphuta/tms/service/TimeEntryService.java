package net.sphuta.tms.service;

import net.sphuta.tms.dto.TimeEntryDtos;
import java.util.UUID;

/**
 * Service interface for managing Time Entries.
 *
 * <p>This interface defines the contract for creating and deleting time entries.
 * Business logic, validations, and logging will be handled in the implementing class
 * (e.g., {@code TimeEntryServiceImpl}).
 *
 * <p>Typical responsibilities:
 * <ul>
 *   <li>Create a new time entry associated with a timesheet.</li>
 *   <li>Delete an existing time entry by its unique identifier.</li>
 *   <li>Ensure validation (e.g., hours > 0, entryDate within timesheet period).</li>
 *   <li>Log method entry/exit and important decisions in the implementation.</li>
 * </ul>
 */
public interface TimeEntryService {

    /**
     * Create a new time entry for a timesheet.
     *
     * @param req DTO containing all necessary information to create the entry
     *            (timesheetId, entryDate, description, hours, rateAtEntry).
     *            <br>
     *            <strong>Note:</strong> Start and end times are auto-computed by the implementation
     *            and should not be passed in the request.
     * @return TimeEntryResponse DTO containing the persisted entry with computed values
     *         such as costAtEntry, startTime, and endTime.
     *
     * <p><b>Expected logging in implementation:</b>
     * <ul>
     *   <li>DEBUG: When method entry occurs with input request values.</li>
     *   <li>INFO: When a time entry is successfully created with ID.</li>
     *   <li>ERROR: If validation fails or timesheet not found.</li>
     * </ul>
     */
    TimeEntryDtos.TimeEntryResponse create(TimeEntryDtos.TimeEntryCreateRequest req);

    /**
     * Delete a time entry by its unique identifier.
     *
     * @param id UUID of the time entry to be deleted.
     *
     * <p><b>Expected logging in implementation:</b>
     * <ul>
     *   <li>DEBUG: When delete is requested for a given ID.</li>
     *   <li>INFO: When a time entry is successfully deleted.</li>
     *   <li>WARN: If entry not found or already invoiced (cannot delete).</li>
     *   <li>ERROR: For unexpected issues during deletion.</li>
     * </ul>
     */
    void delete(UUID id);
}
