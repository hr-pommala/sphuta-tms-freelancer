package net.sphuta.tms.freelancer.service;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.InvoiceLineDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.entity.InvoiceEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsInvoiceRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating invoices (PDF format) from approved timesheets
 * and either emailing them to clients (if SMTP configured) or saving them to a local folder.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Fetches approved timesheets grouped by client</li>
 *   <li>Aggregates time entries into invoice lines</li>
 *   <li>Generates PDF invoices using JasperReports</li>
 *   <li>Sends invoice PDFs via email or saves locally</li>
 *   <li>Persists invoice metadata to database</li>
 * </ul>
 */
@Service
@Slf4j
@Transactional
public class InvoiceGeneratorService {

    // ==========================
    // Repositories and Services
    // ==========================

    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    @Autowired
    private TmsTimeEntryRepository timeEntryRepo;

    @Autowired
    private TmsClientRepository clientRepo;

    @Autowired
    private TmsInvoiceRepository invoiceRepo;

    // Optional: JavaMailSender is injected only if available (avoids failure when email disabled)
    @Autowired(required = false)
    private JavaMailSender mailSender;

    // ==========================
    // Configuration Properties
    // ==========================

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${invoice.output-dir:target/invoices}")
    private String outputDir;

    // =====================================================
    // MAIN METHOD: Generate and send invoices to clients
    // =====================================================

    /**
     * Generates invoices for all approved timesheets and performs one of two actions:
     * <ul>
     *   <li>Sends the generated PDF via email if SMTP is configured</li>
     *   <li>Otherwise saves the invoice to the local disk</li>
     * </ul>
     *
     * @return A summary report of all invoices processed, emailed, saved, or skipped.
     */
    public InvoiceGenerationReport generateAndSendForApprovedTimesheets() {
        log.info("Invoice generation started");

        // Step 1: Fetch all approved timesheets from repository
        List<TimesheetEntity> approved = timesheetRepo.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.APPROVED)
                .collect(Collectors.toList());

        InvoiceGenerationReport report = new InvoiceGenerationReport();
        report.setTotalApprovedTimesheets(approved.size());

        // Step 2: Ensure output directory exists
        try {
            Path out = Path.of(outputDir);
            if (!Files.exists(out)) Files.createDirectories(out);
        } catch (Exception e) {
            log.warn("Failed to create output dir {}: {}", outputDir, e.getMessage());
        }

        // Step 3: Check if email functionality is available
        boolean canSendEmail = (mailSender != null)
                && mailFrom != null && !mailFrom.isBlank()
                && mailHost != null && !mailHost.isBlank()
                && !"smtp.example.com".equals(mailHost);

        log.info("Email available: {} (mailHost={} mailFrom={})", canSendEmail, mailHost, mailFrom);

        int generated = 0;

        // Step 4: Group all approved timesheets by client for batch processing
        Map<Integer, List<TimesheetEntity>> byClient = approved.stream()
                .filter(ts -> ts.getProject() != null && ts.getProject().getClientEntity() != null)
                .collect(Collectors.groupingBy(ts -> ts.getProject().getClientEntity().getId()));

        // ========================================
        // Process each client’s timesheet group
        // ========================================
        for (Map.Entry<Integer, List<TimesheetEntity>> clientEntry : byClient.entrySet()) {
            List<TimesheetEntity> clientTimesheets = clientEntry.getValue();
            if (clientTimesheets == null || clientTimesheets.isEmpty()) continue;

            TimesheetEntity first = clientTimesheets.get(0);
            ClientEntity client = first.getProject().getClientEntity();

            // Guard: Skip if client missing or has no email
            if (client == null) {
                report.addSkipped(null, "Client missing for grouped timesheets");
                continue;
            }
            if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
                report.addSkipped(first.getId(), "Client has no email");
                log.warn("Client {} has no email, skipping invoice generation", client.getId());
                continue;
            }

            try {
                // ================================
                // Step 5: Build invoice line items
                // ================================
                List<InvoiceLineDto> lines = new ArrayList<>();
                BigDecimal totalHours = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal defaultHourlyRate = BigDecimal.ZERO;

                LocalDate periodStart = null;
                LocalDate periodEnd = null;

                // Collect all unique project names for the client
                Set<String> projectNames = clientTimesheets.stream()
                        .map(ts -> ts.getProject() != null ? ts.getProject().getName() : null)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                // Loop through each timesheet to calculate amounts
                for (TimesheetEntity ts : clientTimesheets) {
                    if (ts.getProject() != null && ts.getProject().getHourlyRate() != null) {
                        defaultHourlyRate = ts.getProject().getHourlyRate();
                    }

                    // Determine billing period boundaries
                    if (periodStart == null || (ts.getPeriodStart() != null && ts.getPeriodStart().isBefore(periodStart))) {
                        periodStart = ts.getPeriodStart();
                    }
                    if (periodEnd == null || (ts.getPeriodEnd() != null && ts.getPeriodEnd().isAfter(periodEnd))) {
                        periodEnd = ts.getPeriodEnd();
                    }

                    // Fetch time entries for this timesheet
                    List<TimeEntryEntity> entries = ts.getEntries() != null ? ts.getEntries() : List.of();

                    // Calculate total hours and rates
                    BigDecimal tsTotalHours = TmsTimesheetMappers.totalHours(ts);
                    tsTotalHours = tsTotalHours != null ? tsTotalHours : BigDecimal.ZERO;

                    BigDecimal tsRate = ts.getProject() != null && ts.getProject().getHourlyRate() != null
                            ? ts.getProject().getHourlyRate()
                            : defaultHourlyRate;

                    // Compute total amount for timesheet
                    BigDecimal tsAmount = BigDecimal.ZERO;
                    if (entries != null && !entries.isEmpty()) {
                        for (TimeEntryEntity e : entries) {
                            BigDecimal hours = e.getHours() != null ? e.getHours() : BigDecimal.ZERO;
                            if (e.getCostAtEntry() != null) {
                                tsAmount = tsAmount.add(e.getCostAtEntry());
                            } else {
                                BigDecimal rate = e.getRateAtEntry() != null ? e.getRateAtEntry()
                                        : (tsRate != null ? tsRate : BigDecimal.ZERO);
                                tsAmount = tsAmount.add(rate.multiply(hours));
                            }
                        }
                    } else {
                        // No entries → compute from hours × rate
                        tsAmount = tsTotalHours.multiply(tsRate != null ? tsRate : BigDecimal.ZERO);
                    }

                    // Skip empty timesheets (zero hours)
                    if (tsTotalHours.compareTo(BigDecimal.ZERO) > 0) {
                        String desc = (ts.getPeriodStart() != null ? ts.getPeriodStart().toString() : "")
                                + " - " + (ts.getPeriodEnd() != null ? ts.getPeriodEnd().toString() : "");
                        String projName = ts.getProject() != null ? ts.getProject().getName() : "";

                        // Build invoice line DTO
                        InvoiceLineDto line = new InvoiceLineDto(
                                ts.getPeriodStart(),
                                desc,
                                tsTotalHours,
                                tsRate != null ? tsRate : BigDecimal.ZERO,
                                tsAmount,
                                projName
                        );
                        lines.add(line);

                        // Update totals
                        totalHours = totalHours.add(tsTotalHours);
                        totalAmount = totalAmount.add(tsAmount);
                    }
                }

                // Guard: Skip clients with no valid lines
                if (lines.isEmpty()) {
                    report.addSkipped(first.getId(), "No time entries for client");
                    log.info("No time entries for client {}, skipping", client.getId());
                    continue;
                }

                // Debug info
                String joinedProjects = String.join(", ", projectNames);
                log.info("Preparing invoice for client id={} name={} projects=[{}] invoiceLines={}",
                        client.getId(), client.getName(), joinedProjects, lines.size());

                // ===============================
                // Step 6: Prepare Jasper template
                // ===============================
                Map<String, Object> params = new HashMap<>();
                params.put("clientName", client.getName());
                params.put("projectName", joinedProjects);
                params.put("periodStart", periodStart);
                params.put("periodEnd", periodEnd);
                params.put("totalHours", totalHours);
                params.put("hourlyRate", defaultHourlyRate);
                params.put("totalAmount", totalAmount);

                JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(lines);

                // Load Jasper JRXML file from resources
                InputStream jrxml = getClass().getResourceAsStream("/templates/invoice_template.jrxml");
                if (jrxml == null) {
                    report.addError("Invoice JRXML template not found on classpath: /templates/invoice_template.jrxml");
                    log.error("Invoice JRXML template not found on classpath: /templates/invoice_template.jrxml");
                    break;
                }

                // Compile, fill, and export the Jasper report
                JasperReport jr = JasperCompileManager.compileReport(jrxml);
                JasperPrint jp = JasperFillManager.fillReport(jr, params, ds);
                byte[] pdf = JasperExportManager.exportReportToPdf(jp);

                // ===============================
                // Step 7: Send or save invoice
                // ===============================
                String filename = "invoice-client-" + client.getId() + ".pdf";

                if (canSendEmail) {
                    try {
                        sendEmailWithAttachment(client.getEmail(), "Invoice", "Please find attached", pdf, filename);
                        report.addEmailed(client.getEmail(), filename);
                    } catch (Exception ex) {
                        String em = "Failed to send email to " + client.getEmail() + ": " + ex.getMessage();
                        report.addError(em);
                        Path p = Path.of(outputDir, filename);
                        Files.write(p, pdf, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        report.addSaved(p.toString());
                        log.warn("Email failed, saved invoice to {}", p);
                    }
                } else {
                    // Email disabled → Save invoice to disk
                    Path p = Path.of(outputDir, filename);
                    Files.write(p, pdf, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    report.addSaved(p.toString());
                    log.info("Saved invoice to {} (email disabled)", p);
                }

                // ===============================
                // Step 8: Persist invoice record
                // ===============================
                InvoiceEntity invoice = InvoiceEntity.builder()
                        .clientId(client.getId())
                        .issueDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(30))
                        .currencyCode(client.getCurrencyCode() != null ? client.getCurrencyCode() : "USD")
                        .status(InvoiceEntity.Status.SENT)
                        .build();

                invoiceRepo.save(invoice);
                generated++;

            } catch (Exception ex) {
                String err = "Failed for client " + client.getId() + ": " + ex.getMessage();
                report.addError(err);
                log.error("Failed to generate/send invoice for client {}: {}", client.getId(), ex.getMessage(), ex);
            }
        }

        // Step 9: Final summary
        report.setGeneratedCount(generated);
        log.info("Invoice generation completed; total generated={}", generated);
        return report;
    }

    // ==============================================
    // HELPER METHOD: Send email with PDF attachment
    // ==============================================

    /**
     * Sends a PDF invoice as an email attachment to the client.
     *
     * @param to        Recipient email address
     * @param subject   Email subject line
     * @param body      Email body content
     * @param pdfBytes  PDF file as byte array
     * @param filename  Filename for attachment
     */
    private void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfBytes, String filename) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            if (mailFrom != null && !mailFrom.isBlank()) helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(filename, new ByteArrayResource(pdfBytes));
            mailSender.send(msg);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
