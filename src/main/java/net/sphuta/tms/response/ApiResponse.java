package net.sphuta.tms.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard API response envelope.
 *
 * <p>Every API endpoint should return this wrapper so that
 * clients receive a consistent response structure whether
 * the call succeeds or fails.</p>
 *
 * <p>Structure:</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },   // when success
 *   "error": null
 * }
 *
 * {
 *   "success": false,
 *   "data": null,
 *   "error": {
 *     "status": 409,
 *     "error": "Conflict",
 *     "message": "Project already exists for this client",
 *     "details": { ... }
 *   }
 * }
 * </pre>
 *
 * <p>Uses Java 17 {@code record} for immutability and brevity.</p>
 *
 * @param <T> the type of the payload in {@code data}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ApiError error) {

    /**
     * Factory method for a successful response.
     *
     * @param data payload to wrap
     * @return standardized success response
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * Factory method for a failed response.
     *
     * @param error error descriptor to wrap
     * @return standardized failure response
     */
    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }

    /**
     * Error payload aligned with <a href="https://datatracker.ietf.org/doc/html/rfc7807">
     * RFC 7807 Problem Details</a> (subset).
     *
     * @param status  HTTP status code
     * @param error   short error type (e.g., "Bad Request", "Conflict")
     * @param message human-readable error description
     * @param details optional object containing validation errors or extra context
     */
    public record ApiError(int status, String error, String message, Object details) { }
}
