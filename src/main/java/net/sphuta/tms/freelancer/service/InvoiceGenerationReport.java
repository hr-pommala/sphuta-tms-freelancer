package net.sphuta.tms.freelancer.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
