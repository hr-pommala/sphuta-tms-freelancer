package net.sphuta.tms.freelancer.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * ==========================================================
 * TmsUninvoicedResponse
 * ==========================================================
 *
 * Response DTO returned when querying for uninvoiced time entries
 * of a client within a date range.
 *
 * Purpose:
 * - Wraps a list of {@link TmsTimeEntrySummary} entries for easy consumption.
 * - Used by the endpoint:
 *   <pre>
 *   GET /api/v1/time-entries/uninvoiced?clientId=...&from=...&to=...
 *   </pre>
 *
 * Notes:
 * - {@code from} and {@code to} define the inclusive date range filter.
 * - {@code entries} contains only APPROVED & uninvoiced entries (service enforces this).
 */
@Builder
public record TmsUninvoicedResponse(

        /** Client ID for which uninvoiced time entries are returned. */
        Integer clientId,

        /** Start date of the requested period (inclusive). */
        LocalDate from,

        /** End date of the requested period (inclusive). */
        LocalDate to,

        /** List of uninvoiced time entries in the given range. */
        List<TmsTimeEntrySummary> entries
) {}
