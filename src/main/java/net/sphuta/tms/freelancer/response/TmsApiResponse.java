package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all endpoints in the Freelancer edition.
 *
 * <p>This class ensures that both successful and error responses
 * follow a consistent schema:</p>
 *
 * <pre>
 * {
 *   "success": true,
 *   "statusCode": 200,
 *   "status": "OK",
 *   "message": "Operation successful",
 *   "data": { ... },
 *   "timestamp": "2025-08-28T14:55:00"
 * }
 *
 * {
 *   "success": false,
 *   "statusCode": 400,
 *   "status": "Bad Request",
 *   "message": "Validation failed",
 *   "data": {
 *       "field1": "must not be null",
 *       "field2": "must not be blank"
 *   },
 *   "timestamp": "2025-08-28T14:56:00"
 * }
 * </pre>
 *
 * @param <T> The type of the payload (success data or validation details)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TmsApiResponse<T>(
        boolean success,
        int statusCode,
        String status,
        String message,
        T data,
        LocalDateTime timestamp
) {

    /**
     * Factory method for a successful response.
     *
     * @param status  HTTP status (e.g. {@link HttpStatus#OK})
     * @param message custom message for context
     * @param data    the response payload
     * @return standardized success response
     */
    public static <T> TmsApiResponse<T> success(HttpStatus status, String message, T data) {
        return new TmsApiResponse<>(
                true,
                status.value(),
                status.getReasonPhrase(),
                message,
                data,
                LocalDateTime.now()
        );
    }

    /**
     * Factory method for a failure/validation error response.
     *
     * @param status  HTTP status (e.g. {@link HttpStatus#BAD_REQUEST})
     * @param message human-readable error message
     * @param errors  validation error details (map of field->error)
     * @return standardized failure response
     */
    public static <T> TmsApiResponse<T> failure(HttpStatus status, String message, T errors) {
        return new TmsApiResponse<>(
                false,
                status.value(),
                status.getReasonPhrase(),
                message,
                errors,
                LocalDateTime.now()
        );
    }
}
