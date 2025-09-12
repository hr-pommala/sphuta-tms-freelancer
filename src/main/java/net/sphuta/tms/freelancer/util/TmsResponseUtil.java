package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.response.TmsApiResponse;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ResponseUtil
 *
 * Utility class for generating standardized API responses across the application.
 * Provides methods for successful responses, validation failures, and generic failures.
 * <p>
 * All responses include a timestamp and HTTP status text.
 */
public class TmsResponseUtil {

    private TmsResponseUtil() {} // Prevent instantiation

    /**
     * Create a successful API response with data.
     *
     * @param message Success message
     * @param data    Response payload
     * @param <T>     Type of the response payload
     * @return SphutaTmsApiResponse with success=true
     */
    public static <T> TmsApiResponse<T> success(String message, T data) {
        return new TmsApiResponse<>(
                true,                       // API call successful
                HttpStatus.OK.value(),       // HTTP 200
                HttpStatus.OK.getReasonPhrase(), // "OK"
                message,                     // Success message
                data,                        // Response data
                LocalDateTime.now()          // Current timestamp
        );
    }

    /**
     * Create a validation failure response with error details.
     *
     * @param errors Map of field names to validation error messages
     * @return SphutaTmsApiResponse with success=false and HTTP 400
     */
    public static TmsApiResponse<Map<String, String>> validationFailure(Map<String, String> errors) {
        return new TmsApiResponse<>(
                false,                         // API call failed
                HttpStatus.BAD_REQUEST.value(), // HTTP 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(), // "Bad Request"
                "Validation failed",           // Standard validation failure message
                errors,                        // Field-specific validation errors
                LocalDateTime.now()             // Current timestamp
        );
    }

    /**
     * Create a generic failure response with custom HTTP status and message.
     *
     * @param status  HTTP status code
     * @param message Error message
     * @param <T>     Type of the response payload (usually null)
     * @return SphutaTmsApiResponse with success=false
     */
    public static <T> TmsApiResponse<T> failure(HttpStatus status, String message) {
        return new TmsApiResponse<>(
                false,                        // API call failed
                status.value(),               // Provided HTTP status code
                status.getReasonPhrase(),     // HTTP status text, e.g., "Bad Request"
                message,                       // Error message
                null,                          // No data for failures
                LocalDateTime.now()           // Current timestamp
        );
    }

    /**
     * Optional: Create a "Created" response specifically for POST requests.
     *
     * @param message Success message
     * @param data    Response payload
     * @param <T>     Type of the response payload
     * @return SphutaTmsApiResponse with success=true and HTTP 201
     */
    public static <T> TmsApiResponse<T> created(String message, T data) {
        return new TmsApiResponse<>(
                true,
                HttpStatus.CREATED.value(),           // HTTP 201
                HttpStatus.CREATED.getReasonPhrase(), // "Created"
                message,
                data,
                LocalDateTime.now()
        );
    }
    
}
