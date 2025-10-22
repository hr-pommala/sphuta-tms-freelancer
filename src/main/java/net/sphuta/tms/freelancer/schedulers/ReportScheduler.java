package net.sphuta.tms.freelancer.schedulers;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.repository.InvoiceRepository;
import net.sphuta.tms.freelancer.service.ReportService;
import net.sphuta.tms.freelancer.util.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ===============================================================
 * ReportScheduler
 * ===============================================================
 * Schedules tasks to generate and send invoice reports
 * at specified intervals:
 *
 * 1️⃣ Daily at 12:00 AM
 * 2️⃣ Last working day of the month at 12:00 AM
 * 3️⃣ Last working day of the week (Friday) at 12:00 AM
 * 4️⃣ Mid-month (15th) at 12:00 AM
 *
 * ✅ Uses ReportService to handle PDF generation and email sending
 * ✅ Fetches DRAFT invoices, processes them, and updates status to SENT
 * ===============================================================
 */
@Slf4j
@Component
public class ReportScheduler {

    @Autowired
    private ReportService reportService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * 1️⃣ Every day at 12:00 AM
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyReport() {
        log.info("Daily Report Scheduler started at {}", LocalDateTime.now());
        generateAndSendInvoices("DAILY");
    }

    /**
     * 2️⃣ Last working day of the month at 12:00 AM
     * If last day is weekend, move to previous Friday
     */
    @Scheduled(cron = "0 0 0 L * *") // runs on last day of month
    public void lastWorkingDayOfMonthReport() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY) today = today.minusDays(1);
        else if (dayOfWeek == DayOfWeek.SUNDAY) today = today.minusDays(2);

        log.info("Last Working Day of Month Scheduler triggered for date {}", today);
        generateAndSendInvoices("LAST_WORKING_DAY_MONTH");
    }

    /**
     * 3️⃣ Last working day of the week (Friday) at 12:00 AM
     */
    @Scheduled(cron = "0 0 0 * * FRI")
    public void lastWorkingDayOfWeekReport() {
        log.info("Last Working Day of Week Scheduler triggered at {}", LocalDateTime.now());
        generateAndSendInvoices("LAST_WORKING_DAY_WEEK");
    }

    /**
     * 4️⃣ Middle of the month (15th) at 12:00 AM
     */
    @Scheduled(cron = "0 0 0 15 * *")
    public void midMonthReport() {
        log.info("Mid Month Scheduler triggered at {}", LocalDateTime.now());
        generateAndSendInvoices("MID_MONTH");
    }

    /**
     * ===============================================================
     * Fetches DRAFT invoices from DB, maps them to DTOs, generates PDFs,
     * sends emails, and updates status to SENT.
     * ===============================================================
     *
     * @param scheduleType Type of schedule for logging
     */
    private void generateAndSendInvoices(String scheduleType) {
        try {
            log.info("Scheduler '{}' started", scheduleType);

            // Fetch all invoices with status DRAFT
            List<Invoice> invoices = invoiceRepository.findByStatus("DRAFT");

            if (invoices.isEmpty()) {
                log.info("No invoices to process for '{}'", scheduleType);
                return;
            }

            // Map to DTOs and generate/send PDF
            for (Invoice invoice : invoices) {
                InvoicePayload payload = InvoiceMapper.toPayload(invoice);
                reportService.generateInvoicePdf(payload); // PDF generation + email
                invoice.setStatus("SENT"); // update status
                invoiceRepository.save(invoice);
                log.info("Invoice '{}' processed and status updated", invoice.getInvoiceNo());
            }

            log.info("Scheduler '{}' completed", scheduleType);

        } catch (Exception e) {
            log.error("Error processing invoices in scheduler '{}': {}", scheduleType, e.getMessage(), e);
        }
    }
}
