package net.sphuta.tms.freelancer.response;

/**
 * Standard API response wrapper.
 *
 * @param status  Status of the API response (success / error)
 * @param message Message describing the result
 * @param data    Data returned by the API
 * @param <T>     the type of the response data
 */
public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    /**
     * Factory method for successful response.
     *
     * @param message response message
     * @param data    response data
     * @param <T>     data type
     * @return ApiResponse with status 'success'
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    /**
     * Factory method for error response.
     *
     * @param message error message
     * @param <T>     data type
     * @return ApiResponse with status 'error'
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}
