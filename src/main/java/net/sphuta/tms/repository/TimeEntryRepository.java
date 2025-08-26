package net.sphuta.tms.repository;

import net.sphuta.tms.entity.TimeEntry;
import net.sphuta.tms.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link TimeEntry} entities.
 *
 * <p><strong>Purpose:</strong> Data access layer for time entry records, supporting
 * lookups by parent {@link Timesheet} and by a composite of timesheet + entry date + description.</p>
 *
 * <p><strong>Logging guidance (non-invasive):</strong> Because this is a Spring Data interface,
 * we avoid adding logger fields here. Instead, log at the <em>service layer</em> (caller) or use an
 * AOP interceptor. Suggested logs per method are documented in block comments below without changing code.</p>
 *
 * <p><strong>Thread-safety:</strong> Spring Data repository instances are thread-safe and managed by Spring.</p>
 */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Find all {@link TimeEntry} records belonging to the given {@link Timesheet}.
     *
     * @param timesheet the parent timesheet (required, not null)
     * @return list of matching time entries (possibly empty, never null)
     *
     * <pre>
     * // LOGGING (at caller or via AOP):
     * //  - DEBUG: "Fetching entries for timesheetId={}"
     * //  - TRACE: "DAO call findByTimesheet(tId={}) started"
     * //  - TRACE: "DAO call findByTimesheet(tId={}) completed, resultCount={}"
     * //  - ERROR: log exceptions at service boundary if thrown by the persistence provider
     * </pre>
     */
    List<TimeEntry> findByTimesheet(Timesheet timesheet);

    /**
     * Find a single {@link TimeEntry} by timesheet, entry date, and description.
     * This is useful for idempotent upsert logic (treating date+description as a natural key).
     *
     * @param t     the parent timesheet (required, not null)
     * @param d     the entry date to match (required, not null)
     * @param desc  the description to match (required, not null/empty)
     * @return optional time entry; {@link Optional#empty()} if none found
     *
     * <pre>
     * // LOGGING (at caller or via AOP):
     * //  - DEBUG: "Lookup entry by (timesheetId={}, date={}, desc='{}')"
     * //  - TRACE: "DAO call findByTimesheetAndEntryDateAndDescription(...) started"
     * //  - TRACE: "DAO call ... completed, found={}"  // found is boolean
     * //  - WARN : "Multiple rows matched natural key" // if you ever enforce uniqueness and detect anomalies upstream
     * //  - ERROR: log exceptions at service boundary if a data access error occurs
     * </pre>
     */
    Optional<TimeEntry> findByTimesheetAndEntryDateAndDescription(Timesheet t, LocalDate d, String desc);
}
