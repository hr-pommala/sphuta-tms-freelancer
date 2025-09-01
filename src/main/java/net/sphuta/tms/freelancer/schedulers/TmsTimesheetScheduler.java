package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler class responsible for performing periodic tasks related to timesheets.
 *
 * <p>This class demonstrates scheduled background jobs in the application.
 * The defined job fetches and logs the count of approved timesheets every hour.
 */
@Slf4j
@Component
@EnableScheduling
public class TmsTimesheetScheduler {

    /**
     * Repository dependency for accessing timesheet records.
     * It is injected automatically by Spring using {@link Autowired}.
     */
    @Autowired
    private TmsTimesheetRepository repo;

    /**
     * Scheduled method that runs every hour (at the top of the hour).
     * <p>
     * The cron expression {@code "0 0 * * * *"} means:
     * <ul>
     *   <li>Second = 0</li>
     *   <li>Minute = 0</li>
     *   <li>Hour = Every hour</li>
     *   <li>Day of month = Every day</li>
     *   <li>Month = Every month</li>
     *   <li>Day of week = Every day</li>
     * </ul>
     * This ensures the method is triggered once every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void reportApproved() {
        // Log start of the scheduled task
        log.info("Scheduled task started: reportApproved");

        // Count the number of timesheets with APPROVED status
        long count = repo.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.APPROVED)
                .count();

        // Log the count of approved timesheets
        log.info("Approved timesheets count: {}", count);

        // Log completion of the scheduled task
        log.info("Scheduled task completed: reportApproved");
    }
}
