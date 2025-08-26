package net.sphuta.tms.freelancer.response;

/**
 * Generic API response wrapper for consistent responses.
 * <p>
 * This record is used to standardize API responses across the system
 * by including success status, an optional message, and data payload.
 *
 * @param <T> the type of the data payload returned in the response
 */
public record ApiResponse<T>(
        /** Flag indicating whether the operation was successful (true) or failed (false). */
        boolean success,

        /** A human-readable message that may accompany the response (e.g., error or info). */
        String message,

        /** The actual data payload being returned (can be any type, or null if not applicable). */
        T data
) {

    /**
     * Factory method for a successful response with data only.
     *
     * @param data the data payload
     * @param <T>  the type of the payload
     * @return a standardized {@link ApiResponse} with success = true
     */
    public static <T> ApiResponse<T> ok(T data) {
        // Creates a success response with default "OK" message
        return new ApiResponse<>(true, "OK", data);
    }

    /**
     * Factory method for a successful response with both data and a message.
     *
     * @param data    the data payload
     * @param message the success message to include
     * @param <T>     the type of the payload
     * @return a standardized {@link ApiResponse} with success = true and message set
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        // Creates a success response with both data and message
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Factory method for a successful response with only a message.
     *
     * @param message the success message to include
     * @param <T>     the type parameter (no data payload is provided here)
     * @return a standardized {@link ApiResponse} with success = true and message set
     */
    public static <T> ApiResponse<T> message(String message) {
        // Creates a success response with a message but no data
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Factory method for a failed response with an error message.
     *
     * @param message the error message describing the failure
     * @param <T>     the type parameter (no data payload is provided here)
     * @return a standardized {@link ApiResponse} with success = false and message set
     */
    public static <T> ApiResponse<T> fail(String message) {
        // Creates a failure response with a message but no data
        return new ApiResponse<>(false, message, null);
    }
}
