package net.sphuta.tms.freelancer.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ==========================================================
 * TmsTimeEntrySummary
 * ==========================================================
 *
 * Lightweight DTO summarizing a time entry.
 *
 * Purpose:
 * - Used inside higher-level responses (e.g., {@link TmsUninvoicedResponse})
 *   to avoid exposing full time entry details.
 * - Typically used when listing **uninvoiced** or **approved** time entries.
 *
 * Notes:
 * - Keeps hours as {@link BigDecimal} for precision (e.g., 3.5 hours).
 * - {@code status} expected values: APPROVED, PENDING, REJECTED, etc.
 */
@Builder
public record TmsTimeEntrySummary(

        /** Unique identifier of the time entry. */
        Integer id,

        /** Client ID associated with this time entry. */
        Integer clientId,

        /** Date when the work was performed. */
        LocalDate entryDate,

        /** Number of hours worked (fractional allowed, e.g., 3.5). */
        BigDecimal hours,

        /** Current status of the entry (e.g., APPROVED, PENDING). */
        String status
) {}
