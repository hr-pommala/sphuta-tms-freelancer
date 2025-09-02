package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TmsApiResponse<T>(
        boolean success,
        int statusCode,
        String status,
        String message,
        T data,
        LocalDateTime timestamp
) {

    /** Success response with message and data */
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

    /** Success response with data only */
    public static <T> TmsApiResponse<T> success(HttpStatus status, T data) {
        return success(status, status.getReasonPhrase(), data);
    }

    /** Success response with message only */
    public static <T> TmsApiResponse<T> success(HttpStatus status, String message) {
        return success(status, message, null);
    }

    /** Failure response with message and optional error details */
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

    /** Error response with message only (data is null) */
    public static <T> TmsApiResponse<T> error(HttpStatus status, String message) {
        return failure(status, message, null);
    }

    /** Validation error with field-specific details */
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
