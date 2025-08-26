package net.sphuta.tms.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Example scheduler component for Sphuta TMS.
 *
 * <p>This bean is annotated with {@link EnableScheduling} and can
 * host recurring background jobs such as:</p>
 * <ul>
 *   <li>Nightly maintenance tasks</li>
 *   <li>Data cleanup or archival</li>
 *   <li>Reporting and metrics aggregation</li>
 * </ul>
 *
 * <p>Currently, it contains only a lightweight heartbeat to verify
 * that the scheduling subsystem is running correctly.</p>
 *
 * <p><strong>Notes:</strong></p>
 * <ul>
 *   <li>Logs at DEBUG level to avoid polluting production logs.</li>
 *   <li>Cron expression {@code 0 0 * * * *} = top of every hour.</li>
 * </ul>
 */
@Slf4j
@Component
@EnableScheduling
public class TmsScheduler {

    /**
     * Scheduler heartbeat.
     *
     * <p>Runs once every hour at minute 0 (e.g., 01:00, 02:00, 03:00).
     * Intended as a simple “liveness” indicator for scheduled jobs.</p>
     *
     * <p>You can replace or extend this method with actual scheduled
     * business logic as needed.</p>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void heartbeat() {
        log.debug("Scheduler heartbeat executed successfully at the top of the hour");
    }
}
