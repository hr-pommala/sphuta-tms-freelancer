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

@Service
@Slf4j
@Transactional
public class InvoiceGeneratorService {

    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    @Autowired
    private TmsTimeEntryRepository timeEntryRepo;

    @Autowired
    private TmsClientRepository clientRepo;

    @Autowired
    private TmsInvoiceRepository invoiceRepo;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${invoice.output-dir:target/invoices}")
    private String outputDir;

    /**
     * Generate invoices (PDF) for approved timesheets and either email them (if SMTP configured)
     * or save them to disk (useful for testing). Returns a detailed report explaining actions taken.
     */
    public InvoiceGenerationReport generateAndSendForApprovedTimesheets() {
        log.info("Invoice generation started");
        List<TimesheetEntity> approved = timesheetRepo.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.APPROVED)
                .collect(Collectors.toList());

        InvoiceGenerationReport report = new InvoiceGenerationReport();
        report.setTotalApprovedTimesheets(approved.size());

        // ensure output dir exists for saving PDFs
        try {
            Path out = Path.of(outputDir);
            if (!Files.exists(out)) Files.createDirectories(out);
        } catch (Exception e) {
            log.warn("Failed to create output dir {}: {}", outputDir, e.getMessage());
        }

        boolean canSendEmail = (mailSender != null) && mailFrom != null && !mailFrom.isBlank() && mailHost != null && !mailHost.isBlank() && !"smtp.example.com".equals(mailHost);
        log.info("Email available: {} (mailHost={} mailFrom={})", canSendEmail, mailHost, mailFrom);

        int generated = 0;

        // Group approved timesheets by client id (skip timesheets missing project/client)
        Map<Integer, List<TimesheetEntity>> byClient = approved.stream()
                .filter(ts -> ts.getProject() != null && ts.getProject().getClientEntity() != null)
                .collect(Collectors.groupingBy(ts -> ts.getProject().getClientEntity().getId()));

        for (Map.Entry<Integer, List<TimesheetEntity>> clientEntry : byClient.entrySet()) {
            List<TimesheetEntity> clientTimesheets = clientEntry.getValue();
            if (clientTimesheets == null || clientTimesheets.isEmpty()) continue;

            TimesheetEntity first = clientTimesheets.get(0);
            ClientEntity client = first.getProject().getClientEntity();
            if (client == null) {
                // shouldn't happen due to filter, but guard anyway
                report.addSkipped(null, "Client missing for grouped timesheets");
                continue;
            }

            if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
                report.addSkipped(first.getId(), "Client has no email");
                log.warn("Client {} has no email, skipping invoice generation", client.getId());
                continue;
            }

            try {
                List<InvoiceLineDto> lines = new ArrayList<>();
                BigDecimal totalHours = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal defaultHourlyRate = BigDecimal.ZERO;

                LocalDate periodStart = null;
                LocalDate periodEnd = null;

                // collect entries across all timesheets for this client
                Set<String> projectNames = clientTimesheets.stream()
                        .map(ts -> ts.getProject() != null ? ts.getProject().getName() : null)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (TimesheetEntity ts : clientTimesheets) {
                    if (ts.getProject() != null && ts.getProject().getHourlyRate() != null) {
                        defaultHourlyRate = ts.getProject().getHourlyRate();
                    }

                    if (periodStart == null || (ts.getPeriodStart() != null && ts.getPeriodStart().isBefore(periodStart))) {
                        periodStart = ts.getPeriodStart();
                    }
                    if (periodEnd == null || (ts.getPeriodEnd() != null && ts.getPeriodEnd().isAfter(periodEnd))) {
                        periodEnd = ts.getPeriodEnd();
                    }

                    // Use the timesheet's entries to compute a single invoice line per timesheet
                    List<TimeEntryEntity> entries = ts.getEntries() != null ? ts.getEntries() : List.of();

                    // Compute timesheet-level totals (use mapper to sum entries' hours)
                    BigDecimal tsTotalHours = TmsTimesheetMappers.totalHours(ts);
                    tsTotalHours = tsTotalHours != null ? tsTotalHours : BigDecimal.ZERO;

                    // Determine hourly rate to use for this timesheet (project rate or default)
                    BigDecimal tsRate = ts.getProject() != null && ts.getProject().getHourlyRate() != null ? ts.getProject().getHourlyRate() : defaultHourlyRate;

                    // Compute timesheet-level amount: prefer per-entry cost overrides, otherwise rate * hours
                    BigDecimal tsAmount = BigDecimal.ZERO;
                    if (entries != null && !entries.isEmpty()) {
                        for (TimeEntryEntity e : entries) {
                            BigDecimal hours = e.getHours() != null ? e.getHours() : BigDecimal.ZERO;
                            if (e.getCostAtEntry() != null) {
                                tsAmount = tsAmount.add(e.getCostAtEntry());
                            } else {
                                BigDecimal rate = e.getRateAtEntry() != null ? e.getRateAtEntry() : (tsRate != null ? tsRate : BigDecimal.ZERO);
                                tsAmount = tsAmount.add(rate.multiply(hours));
                            }
                        }
                    } else {
                        // No entries listed on timesheet; compute amount from total hours * tsRate
                        tsAmount = tsTotalHours.multiply(tsRate != null ? tsRate : BigDecimal.ZERO);
                    }

                    // Only include timesheets that have non-zero hours (skip empty timesheets)
                    if (tsTotalHours.compareTo(BigDecimal.ZERO) > 0) {
                        String desc = (ts.getPeriodStart() != null ? ts.getPeriodStart().toString() : "")
                                + " - " + (ts.getPeriodEnd() != null ? ts.getPeriodEnd().toString() : "");
                        String projName = ts.getProject() != null ? ts.getProject().getName() : "";

                        InvoiceLineDto line = new InvoiceLineDto(
                                ts.getPeriodStart(),
                                desc,
                                tsTotalHours,
                                tsRate != null ? tsRate : BigDecimal.ZERO,
                                tsAmount,
                                projName
                        );
                        lines.add(line);

                        // Accumulate grand totals
                        totalHours = totalHours.add(tsTotalHours);
                        totalAmount = totalAmount.add(tsAmount);
                    }
                }

                if (lines.isEmpty()) {
                    report.addSkipped(first.getId(), "No time entries for client");
                    log.info("No time entries for client {}, skipping", client.getId());
                    continue;
                }

                // Debug: log which projects are included and how many lines we will render
                String joinedProjects = String.join(", ", projectNames);
                log.info("Preparing invoice for client id={} name={} projects=[{}] invoiceLines={}", client.getId(), client.getName(), joinedProjects, lines.size());

                Map<String, Object> params = new HashMap<>();
                params.put("clientName", client.getName());
                params.put("projectName", joinedProjects);
                params.put("periodStart", periodStart);
                params.put("periodEnd", periodEnd);
                params.put("totalHours", totalHours);
                params.put("hourlyRate", defaultHourlyRate);
                params.put("totalAmount", totalAmount);

                JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(lines);

                InputStream jrxml = getClass().getResourceAsStream("/templates/invoice_template.jrxml");
                if (jrxml == null) {
                    report.addError("Invoice JRXML template not found on classpath: /templates/invoice_template.jrxml");
                    log.error("Invoice JRXML template not found on classpath: /templates/invoice_template.jrxml");
                    break;
                }
                JasperReport jr = JasperCompileManager.compileReport(jrxml);
                JasperPrint jp = JasperFillManager.fillReport(jr, params, ds);
                byte[] pdf = JasperExportManager.exportReportToPdf(jp);

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
                    Path p = Path.of(outputDir, filename);
                    Files.write(p, pdf, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    report.addSaved(p.toString());
                    log.info("Saved invoice to {} (email disabled)", p);
                }

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

        report.setGeneratedCount(generated);
        log.info("Invoice generation completed; total generated={}", generated);
        return report;
    }

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
