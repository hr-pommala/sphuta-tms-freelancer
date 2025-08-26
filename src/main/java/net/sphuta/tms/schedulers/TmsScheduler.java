package net.sphuta.tms.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TmsScheduler {
    @Scheduled(fixedRate = 60000)
    public void runScheduledTask() {
        // Scheduled task logic for TMS can be added here
    }
}

