package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.service.InvoiceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

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

    //  Run every day at 12:00 AM
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyInvoiceJob() {
        log.info("Daily invoice scheduler triggered (12:00 AM)");
        try {
            invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        } catch (Exception ex) {
            log.error("Error while running daily invoice generator: {}", ex.getMessage(), ex);
        }
    }

    // Run on the last day of the week (Sunday midnight)
    // Cron: At 12:00 AM every Monday (so it processes Sunday’s data)
    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyInvoiceJob() {
        log.info("Weekly invoice scheduler triggered (Monday 12:00 AM for previous week)");
        try {
            invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        } catch (Exception ex) {
            log.error("Error while running weekly invoice generator: {}", ex.getMessage(), ex);
        }
    }

    //  Run on 15th day of every month at 12:00 AM (Month Mid)
    @Scheduled(cron = "0 0 0 15 * *")
    public void runMidMonthInvoiceJob() {
        log.info("Mid-month invoice scheduler triggered (15th 12:00 AM)");
        try {
            invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        } catch (Exception ex) {
            log.error("Error while running mid-month invoice generator: {}", ex.getMessage(), ex);
        }
    }

    //  Run on last working day of the month at 12:00 AM
    // Logic-based trigger (not pure cron) to handle weekends properly.
    @Scheduled(cron = "0 0 0 * * *") // runs daily at midnight and checks condition
    public void runMonthEndInvoiceJob() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Find previous working day if last day is weekend
        LocalDate targetDay = lastDayOfMonth;
        if (lastDayOfMonth.getDayOfWeek() == DayOfWeek.SATURDAY) {
            targetDay = lastDayOfMonth.minusDays(1);
        } else if (lastDayOfMonth.getDayOfWeek() == DayOfWeek.SUNDAY) {
            targetDay = lastDayOfMonth.minusDays(2);
        }

        if (today.equals(targetDay)) {
            log.info("Month-end invoice scheduler triggered (Last working day 12:00 AM)");
            try {
                invoiceGeneratorService.generateAndSendForApprovedTimesheets();
            } catch (Exception ex) {
                log.error("Error while running month-end invoice generator: {}", ex.getMessage(), ex);
            }
        }
    }
}

