package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code TmsScheduler} – Application-wide scheduled task manager.
 *
 * <p>This class demonstrates how to configure scheduled tasks using Spring’s
 * {@link Scheduled} annotation. It is annotated with
 * {@link EnableScheduling} to ensure scheduling is enabled in the application context,
 * and {@link Component} so Spring can discover and instantiate it.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Provides a lightweight "heartbeat" log that runs on a schedule.</li>
 *   <li>Confirms that the scheduling subsystem of the app is active and working.</li>
 *   <li>Can be extended to add more cron-based or fixed-rate/fixed-delay jobs.</li>
 * </ul>
 */
@Slf4j
@Component
@EnableScheduling
public class TmsScheduler {

    /**
     * Heartbeat task that executes once every hour on the hour.
     *
     * <p>Cron expression {@code "0 0 * * * *"} means:</p>
     * <ul>
     *   <li>Second = 0</li>
     *   <li>Minute = 0</li>
     *   <li>Hour = Every hour</li>
     *   <li>Day of month = Every day</li>
     *   <li>Month = Every month</li>
     *   <li>Day of week = Every day</li>
     * </ul>
     *
     * <p>This produces an execution schedule like:
     * 01:00, 02:00, 03:00, … up to 23:00 daily.</p>
     *
     * <p>Logging:</p>
     * Logs a DEBUG-level message each time it runs,
     * so the application log can be monitored for system liveness.</p>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void heartbeat() {
        log.debug("Scheduler heartbeat: app is running");
    }
}
