//package net.sphuta.tms.freelancer.util;
//
//import net.sphuta.tms.freelancer.dto.TmsTimeEntrySummary;
//import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
//
///**
// * ==========================================================
// * TmsTimeEntryMapper
// * ==========================================================
// *
// * Utility class for converting between:
// * - {@link TimeEntryEntity} (JPA entity),
// * - {@link TmsTimeEntrySummary} (lightweight DTO for API responses).
// *
// * Design:
// * - Pure static methods for stateless mapping.
// * - Keeps mapping logic out of services/controllers.
// *
// * Typical usage:
// * - Transform {@link TimeEntryEntity} into {@link TmsTimeEntrySummary}
// *   when returning uninvoiced entries in {@code TmsUninvoicedResponse}.
// */
//public class TmsTimeEntryMapper {
//
//    /**
//     * Converts a {@link TimeEntryEntity} into a {@link TmsTimeEntrySummary}.
//     *
//     * Purpose:
//     * - Provides a lightweight, read-only representation of time entries
//     *   for API responses (e.g., uninvoiced entries).
//     *
//     * Notes:
//     * - Converts enum {@code Status} into a string for transport.
//     * - Excludes heavy audit fields (createdAt, updatedAt).
//     *
//     * @param t the JPA entity
//     * @return summary DTO
//     */
//    public static TmsTimeEntrySummary toSummary(TimeEntryEntity t) {
//        return TmsTimeEntrySummary.builder()
//                .id(t.getId())
//                .clientId(t.getClientId())
//                .entryDate(t.getEntryDate())
//                .hours(t.getHours())
//                .status(t.getStatus().name()) // ✅ expose enum as string
//                .build();
//    }
//}
