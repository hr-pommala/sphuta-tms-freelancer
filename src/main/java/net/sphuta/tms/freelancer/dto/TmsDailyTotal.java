package net.sphuta.tms.freelancer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing the total hours worked for a specific day.
 *
 * <p>This record is typically used in timesheet summaries or reports
 * to aggregate the number of hours logged on a given date.</p>
 */
public record TmsDailyTotal(

        /**
         * The date for which the total hours are calculated.
         */
        LocalDate date,

        /**
         * Total hours worked on the given date.
         *
         * <p>This value represents the sum of all time entries for that date.</p>
         */
        BigDecimal hours
) {}
