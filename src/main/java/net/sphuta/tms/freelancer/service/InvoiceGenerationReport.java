package net.sphuta.tms.freelancer.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Report class to track the results of invoice generation.
 * <p>
 * This class maintains counts and lists of various outcomes during
 * the invoice generation process, including:
 * <ul>
 *   <li>Total approved timesheets processed</li>
 *   <li>Count of successfully generated invoices</li>
 *   <li>List of saved invoice filenames</li>
 *   <li>List of emailed invoices with recipient details</li>
 *   <li>List of skipped timesheets with reasons</li>
 *   <li>List of errors encountered</li>
 * </ul>
 * </p>
 */
@Data
public class InvoiceGenerationReport {
    private int totalApprovedTimesheets;
    private int generatedCount;
    private List<String> saved = new ArrayList<>();
    private List<String> emailed = new ArrayList<>();
    private List<SkippedEntry> skipped = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public void addSaved(String s) { saved.add(s); }
    public void addEmailed(String email, String filename) { emailed.add(email + ":" + filename); }
    public void addSkipped(Integer timesheetId, String reason) { skipped.add(new SkippedEntry(timesheetId, reason)); }
    public void addError(String e) { errors.add(e); }
/*
     * Inner class to represent a skipped timesheet entry.
     * Contains the timesheet ID and the reason for skipping.
     */
    @Data
    public static class SkippedEntry {
        private Integer timesheetId;
        private String reason;

        public SkippedEntry(Integer timesheetId, String reason) {
            this.timesheetId = timesheetId;
            this.reason = reason;
        }
    }
}
