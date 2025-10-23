package net.sphuta.tms.freelancer.controller;

import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.service.InvoiceGenerationReport;
import net.sphuta.tms.freelancer.service.InvoiceGeneratorService;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.util.TmsTimesheetMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    @Autowired
    private TmsClientRepository clientRepo;

    @Autowired
    private TmsProjectRepository projectRepo;

    @Autowired
    private TmsTimesheetRepository timesheetRepo;

    @Autowired
    private TmsTimeEntryRepository timeEntryRepo;

    @PostMapping("/generate")
    public ResponseEntity<InvoiceGenerationReport> generateNow() {
        InvoiceGenerationReport report = invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        return ResponseEntity.ok(report);
    }

    // Development helper: pick existing data (no hard-coded values) and run invoice generation.
    @PostMapping("/seed")
    public ResponseEntity<?> seedAndGenerateFromExisting() {
        // Find an active client with email
        Optional<ClientEntity> clientOpt = clientRepo.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()) && c.getEmail() != null && !c.getEmail().isBlank())
                .findFirst();
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active client with an email found. Please create a client with email and try again.");
        }
        ClientEntity client = clientOpt.get();

        // Find all active projects for that client
        List<ProjectEntity> clientProjects = projectRepo.findAll().stream()
                .filter(p -> p.getClientEntity() != null && p.getClientEntity().getId() == client.getId() && p.isActive())
                .toList();
        if (clientProjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active projects found for client id=" + client.getId() + ". Create a project for the client and try again.");
        }

        // Search across all client projects for an APPROVED timesheet that has hours (or accept any approved timesheet)
        TimesheetEntity chosenTimesheet = null;
        ProjectEntity chosenProject = null;
        for (ProjectEntity p : clientProjects) {
            Optional<TimesheetEntity> tsOpt = timesheetRepo.findAll().stream()
                    .filter(t -> t.getProject() != null && t.getProject().getId() == p.getId() && t.getStatus() == TimesheetStatus.APPROVED)
                    .findFirst();
            if (tsOpt.isPresent()) {
                TimesheetEntity ts = tsOpt.get();
                // Prefer timesheets that have totalHours > 0, otherwise accept the first approved timesheet
                BigDecimal totalHours = TmsTimesheetMappers.totalHours(ts);
                if (totalHours == null) totalHours = BigDecimal.ZERO;
                if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
                    chosenTimesheet = ts;
                    chosenProject = p;
                    break;
                } else {
                    // keep as fallback if no better timesheet found
                    if (chosenTimesheet == null) {
                        chosenTimesheet = ts;
                        chosenProject = p;
                    }
                }
            }
        }

        if (chosenTimesheet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No APPROVED timesheet found across projects for client id=" + client.getId() + ". Approve a timesheet and try again.");
        }

        // Trigger the invoice generator which will pick up approved timesheets (including this one)
        InvoiceGenerationReport report = invoiceGeneratorService.generateAndSendForApprovedTimesheets();
        return ResponseEntity.ok(report);
    }
}
