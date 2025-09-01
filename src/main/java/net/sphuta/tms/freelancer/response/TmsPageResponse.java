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
public record TmsPageResponse<T>(List<T> content, PageMeta page) {

    /**
     * Metadata for a page of results.
     *
     * @param number        current page number (0-based)
     * @param size          the size of the page (number of elements per page)
     * @param totalElements total number of elements across all pages
     * @param totalPages    total number of pages available
     */
    public record PageMeta(int number, int size, long totalElements, int totalPages) { }
}
