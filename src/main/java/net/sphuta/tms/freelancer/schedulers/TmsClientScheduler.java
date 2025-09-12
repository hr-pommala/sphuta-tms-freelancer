package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code TmsClientScheduler} – Centralized scheduler component for running
 * recurring application tasks.
 *
 * <p>
 * This class demonstrates how to configure and run scheduled background
 * jobs in a Spring Boot application using {@link Scheduled}.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Defines recurring scheduled tasks (e.g., heartbeat monitor).</li>
 *   <li>Uses SLF4J logging to confirm the application is alive.</li>
 *   <li>Can be extended to include cleanup jobs, email reminders, etc.</li>
 * </ul>
 *
 * <p>
 * Annotated with:
 * <ul>
 *   <li>{@link Component} – registers this scheduler as a Spring bean.</li>
 *   <li>{@link EnableScheduling} – enables scheduling across the application.</li>
 *   <li>{@link Slf4j} – provides a logger instance for structured logging.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@EnableScheduling
public class TmsClientScheduler {

    /**
     * Heartbeat task that executes once every hour, on the hour.
     *
     * <p>
     * Scheduled with a {@code cron} expression:
     * <ul>
     *   <li>{@code 0 0 * * * *} → second=0, minute=0, every hour, every day.</li>
     *   <li>This ensures the job runs at 1:00, 2:00, 3:00, etc.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Purpose:
     * <ul>
     *   <li>Acts as a health indicator – if logs appear every hour, the app is alive.</li>
     *   <li>Can be extended later to perform lightweight health checks.</li>
     * </ul>
     * </p>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void heartbeat() {
        // Debug-level log, avoids polluting production logs
        log.debug("Scheduler heartbeat: app is running");
    }
}
