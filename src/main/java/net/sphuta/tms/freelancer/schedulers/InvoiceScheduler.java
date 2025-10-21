package net.sphuta.tms.freelancer.schedulers;

import net.sphuta.tms.freelancer.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

/**
 * InvoiceScheduler - centralized scheduler for invoice-related triggers.
 */
@Component
public class InvoiceScheduler {
    private static final Logger log = LoggerFactory.getLogger(InvoiceScheduler.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private final ReportService reportService;

    @Autowired
    public InvoiceScheduler(ReportService reportService) {
        this.reportService = reportService;
        log.info("InvoiceScheduler constructed — bean registered. Zone: {}", ZONE);
    }

    /**
     * Daily at 00:00 Asia/Kolkata.
     * Also checks if today is last working day of month and triggers month-end if so.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void dailyMidnight() {
        log.info("[SCHEDULER][DAILY] Running daily tasks at 00:00 {}", ZONE);
        try {
            reportService.validateAndProcessInvoices("DAILY");

            LocalDate today = LocalDate.now(ZONE);
            if (isLastWorkingDayOfMonth(today)) {
                log.info("[SCHEDULER][MONTH-END] Today {} is last working day of month - running month-end tasks", today);
                reportService.validateAndProcessInvoices("MONTH_END_LAST_WORKING_DAY");
            } else {
                log.debug("[SCHEDULER][MONTH-END] Today {} is not last working day of month", today);
            }
        } catch (Exception ex) {
            log.error("Error in dailyMidnight scheduler: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Weekly - last working day of week is Friday; run Friday at 00:00 Asia/Kolkata.
     */
    @Scheduled(cron = "0 0 0 * * FRI", zone = "Asia/Kolkata")
    public void weeklyLastWorkDay() {
        log.info("[SCHEDULER][WEEKLY] Running weekly last-work-day task (Friday 00:00) {}", ZONE);
        try {
            reportService.validateAndProcessInvoices("WEEKLY_LAST_WORKDAY");
        } catch (Exception ex) {
            log.error("Error in weeklyLastWorkDay scheduler: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Monthly mid — runs on 15th of every month at 00:00 Asia/Kolkata.
     */
    @Scheduled(cron = "0 0 0 15 * *", zone = "Asia/Kolkata")
    public void monthlyMid() {
        log.info("[SCHEDULER][MONTHLY-MID] Running monthly mid (15th) task {}", ZONE);
        try {
            reportService.validateAndProcessInvoices("MONTHLY_MID");
        } catch (Exception ex) {
            log.error("Error in monthlyMid scheduler: {}", ex.getMessage(), ex);
        }
    }

    /*
     * If you want to test locally without waiting:
     * Uncomment one of these (LOCAL TEST ONLY) and restart app.
     *
     * @Scheduled(cron = "0/30 * * * * *", zone = "Asia/Kolkata") // every 30s
     * public void dailyMidnightTest() { ... }
     *
     */

    private boolean isLastWorkingDayOfMonth(LocalDate date) {
        YearMonth ym = YearMonth.of(date.getYear(), date.getMonth());
        LocalDate lastDay = ym.atEndOfMonth();
        DayOfWeek dow = lastDay.getDayOfWeek();

        LocalDate lastWorking;
        if (dow == DayOfWeek.SATURDAY) {
            lastWorking = lastDay.minusDays(1);
        } else if (dow == DayOfWeek.SUNDAY) {
            lastWorking = lastDay.minusDays(2);
        } else {
            lastWorking = lastDay;
        }

        log.debug("Computed lastWorkingDay for {} => lastDay={}, lastWorking={}", ym, lastDay, lastWorking);
        return date.equals(lastWorking);
    }
}
