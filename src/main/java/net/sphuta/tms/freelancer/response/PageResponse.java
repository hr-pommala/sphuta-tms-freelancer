package net.sphuta.tms.freelancer.response;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 * <p>
 * This record carries a list of items (content) and paging metadata (page).
 * </p>
 *
 * @param <T> the type of elements contained in the content list
 */
public record PageResponse<T>(

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
