package net.sphuta.tms.freelancer.schedulers;

import net.sphuta.tms.freelancer.enam.TimesheetStatus;
import net.sphuta.tms.freelancer.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler component that runs background tasks for the TMS system.
 * <p>
 * This class is annotated with {@link EnableScheduling} and {@link Component}
 * so that Spring can auto-detect and run scheduled jobs at runtime.
 * <p>
 * Current behavior:
 * <ul>
 *   <li>Reports the count of timesheets with status {@code APPROVED} every hour.</li>
 * </ul>
 */
@Component
@EnableScheduling
public class TimesheetScheduler {

    /** Logger instance for this scheduler class. */
    private static final Logger log = LoggerFactory.getLogger(TimesheetScheduler.class);

    /** Repository for accessing timesheet data from the database. */
    private final TimesheetRepository repo;

    /**
     * Constructor-based injection for the timesheet repository.
     *
     * @param repo Repository to access and query timesheet records.
     */
    public TimesheetScheduler(TimesheetRepository repo) {
        this.repo = repo;
    }

    /**
     * Scheduled job that runs hourly (at the start of every hour).
     * <p>
     * Uses a cron expression {@code 0 0 * * * *}.
     * <p>
     * Functionality:
     * <ul>
     *   <li>Fetch all timesheets from the repository</li>
     *   <li>Filter those with status APPROVED</li>
     *   <li>Log the count of approved timesheets</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void reportApproved() {
        // Log entry into method
        log.info("Starting scheduled task: reportApproved");

        // Count number of approved timesheets
        long count = repo.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.APPROVED)
                .count();

        // Debug-level log for the actual result
        log.debug("Approved timesheets count: {}", count);

        // Log exit from method
        log.info("Completed scheduled task: reportApproved");
    }
}
