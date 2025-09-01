package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code TmsScheduler} – Application-wide scheduled task manager.
 */
@Slf4j
@Component
@EnableScheduling
public class TmsClientScheduler {

    /** Heartbeat task that executes once every hour on the hour. */
    @Scheduled(cron = "0 0 * * * *")
    public void heartbeat() {
        log.debug("Scheduler heartbeat: app is running");
    }
}
