package net.sphuta.tms.freelancer.response;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * <p>Encapsulates a page of results and associated pagination metadata.
 * Used as the {@code data} portion of {@link TmsApiResponse} when returning
 * lists of resources such as projects or clients.</p>
 *
 * <p>Structure:</p>
 * <pre>
 * {
 *   "content": [ ... ],
 *   "page": {
 *     "number": 0,
 *     "size": 25,
 *     "totalElements": 100,
 *     "totalPages": 4
 *   }
 * }
 * </pre>
 *
 * @param <T> the type of elements contained in the page
 */
public record TmsPageResponse<T>(
        /**
         * The list of items returned in this page.
         * Example: list of timesheets, projects, etc.
         */
        List<T> content,

        /**
         * The metadata object describing pagination details
         * such as page number, size, total elements, and total pages.
         */
        PageMeta page
) {

    /**
     * Metadata about the current page.
     * <p>
     * This inner record describes the current page number,
     * how many elements per page, total elements available,
     * and total number of pages.
     * </p>
     */
    public record PageMeta(
            /** The current page index (zero-based). */
            int number,

            /** The number of elements requested per page. */
            int size,

            /** The total number of elements available across all pages. */
            long totalElements,

            /** The total number of pages available. */
            int totalPages
    ) {}
}
