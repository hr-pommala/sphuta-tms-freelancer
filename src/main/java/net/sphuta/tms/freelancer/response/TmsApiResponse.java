package net.sphuta.tms.freelancer.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ApiResponse is a generic response wrapper used for sending consistent API responses.
 *
 * @param success   whether the operation was successful (true/false)
 * @param statusCode HTTP status code representing the response
 * @param status    human-readable HTTP status (e.g., "OK", "BAD_REQUEST")
 * @param message   custom message describing the result
 * @param data      actual response body (can be any type or null)
 * @param timestamp the time when the response was generated
 *
 * @param <T> the type of the data included in the response
 */
public record TmsApiResponse<T>(
        /** Indicates if the request was processed successfully */
        boolean success,

        /** Numeric HTTP status code (e.g., 200, 400, 404) */
        int statusCode,

        /** HTTP status description (e.g., OK, BAD_REQUEST, NOT_FOUND) */
        String status,

        /** Human-readable message describing the result */
        String message,

        /** Response data of generic type T */
        T data,

        /** Timestamp of when the response was created */
        LocalDateTime timestamp
) {
    /**
     * Creates a success response with custom message and data.
     *
     * @param httpStatus the HTTP status to return
     * @param message    the custom success message
     * @param data       the payload data
     * @param <T>        generic type of data
     * @return ApiResponse with populated fields
     */
    public static <T> TmsApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new TmsApiResponse<>(
                true,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                data,
                LocalDateTime.now()
        );
    }

    /**
     * Creates a success response with data and default status message.
     *
     * @param httpStatus the HTTP status to return
     * @param data       the payload data
     * @param <T>        generic type of data
     * @return ApiResponse with populated fields
     */
    public static <T> TmsApiResponse<T> success(HttpStatus httpStatus, T data) {
        return success(httpStatus, httpStatus.getReasonPhrase(), data);
    }

    /**
     * Creates a success response with only a message (no data).
     *
     * @param httpStatus the HTTP status to return
     * @param message    the custom success message
     * @param <T>        generic type of data
     * @return ApiResponse with populated fields
     */
    public static <T> TmsApiResponse<T> success(HttpStatus httpStatus, String message) {
        return success(httpStatus, message, null);
    }

    /**
     * Creates an error response with custom error message.
     *
     * @param httpStatus the HTTP status to return
     * @param message    the error message
     * @param <T>        generic type of data (usually null for errors)
     * @return ApiResponse with populated fields
     */
    public static <T> TmsApiResponse<T> error(HttpStatus httpStatus, String message) {
        return new TmsApiResponse<>(
                false,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Creates a validation error response with details of field-specific errors.
     *
     * @param fieldErrors a map containing field names and their corresponding error messages
     * @return ApiResponse containing validation error details
     */
    public static TmsApiResponse<Map<String, String>> validationError(Map<String, String> fieldErrors) {
        return new TmsApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                fieldErrors,
                LocalDateTime.now()
        );
    }
}
