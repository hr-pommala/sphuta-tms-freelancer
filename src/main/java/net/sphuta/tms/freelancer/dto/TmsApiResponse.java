package net.sphuta.tms.freelancer.dto;

import lombok.Builder;

/**
 * ==========================================================
 * TmsApiResponse
 * ==========================================================
 *
 * Generic wrapper for **all API responses** returned by the system.
 *
 * Purpose:
 * - Ensures consistency across endpoints by always wrapping
 *   actual response payloads with a message and optional pagination metadata.
 * - Helps clients parse responses predictably.
 *
 * Typical usage:
 * <pre>
 * {
 *   "message": "Clients fetched",
 *   "data": [
 *      { "id": 1, "companyName": "Acme LLC", ... },
 *      { "id": 2, "companyName": "Beta Inc", ... }
 *   ],
 *   "meta": {
 *      "total": 500,
 *      "page": 0,
 *      "size": 25
 *   }
 * }
 * </pre>
 *
 * @param <T> Type of response payload (entity DTO, list, etc.)
 */
@Builder
public record TmsApiResponse<T>(

        /** A user/developer-friendly message, e.g., "Client created" or "Clients fetched". */
        String message,

        /** The actual response payload (may be an object, list, or null for empty responses). */
        T data,

        /** Pagination metadata (only set for paged responses). */
        TmsApiMeta meta
) {
    // No extra methods needed; Lombok @Builder + record gives immutability + convenience.
}
