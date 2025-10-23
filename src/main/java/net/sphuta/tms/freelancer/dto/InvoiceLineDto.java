package net.sphuta.tms.freelancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InvoiceLineDto {
    private LocalDate entryDate;
    private String description;
    private BigDecimal hours;
    private BigDecimal rate;
    private BigDecimal amount;
    private String projectName; // new field to capture project name for invoices that combine multiple projects
}
