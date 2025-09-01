package net.sphuta.tms.freelancer.dto;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ==========================================================
 * TmsApiMeta
 * ==========================================================
 *
 * Metadata record for paginated API responses.
 *
 * Purpose:
 * - Standard wrapper for pagination metadata.
 * - Used inside {@link net.sphuta.tms.freelancer.response.TmsApiResponse}
 *   to give clients context about page navigation.
 *
 * Typical usage:
 * <pre>
 * {
 *   "success": true,
 *   "statusCode": 200,
 *   "message": "Clients fetched",
 *   "data": [...],
 *   "meta": {
 *      "total": 500,
 *      "page": 0,
 *      "size": 25
 *   }
 * }
 * </pre>
 */
@Builder
@Schema(name = "TmsApiMeta", description = "Pagination metadata for API responses")
public record TmsApiMeta(

        /** Total number of items available across all pages. */
        @Schema(description = "Total number of records across all pages", example = "500")
        Long total,

        /** Current page number (0-based). */
        @Schema(description = "Current page index (0-based)", example = "0")
        Integer page,

        /** Number of items requested per page. */
        @Schema(description = "Page size (items per page)", example = "25")
        Integer size
) {
    // No extra methods needed; Lombok @Builder + record gives immutability + convenience.
}
