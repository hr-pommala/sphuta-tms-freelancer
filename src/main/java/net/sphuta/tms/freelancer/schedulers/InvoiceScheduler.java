package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.service.InvoiceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that triggers invoice generation every hour.
 */
@Slf4j
@Component
public class InvoiceScheduler {

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    // Run at the top of every hour
    @Scheduled(cron = "0 * * * * *")
    public void runHourlyInvoiceJob() {
        log.info("Hourly invoice scheduler triggered");
        try {
            invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        } catch (Exception ex) {
            log.error("Error while running invoice generator: {}", ex.getMessage(), ex);
        }
    }
}

