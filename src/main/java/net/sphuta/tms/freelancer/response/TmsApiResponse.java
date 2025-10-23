package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TmsApiResponse<T>(
        boolean success,
        int statusCode,
        String status,
        String message,
        T data,
        LocalDateTime timestamp,
        String traceId
) {

    /** Generate a random trace ID */
    private static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /** Success response with message and data */
    public static <T> TmsApiResponse<T> success(HttpStatus status, String message, T data) {
        return new TmsApiResponse<>(
                true,
                status.value(),
                status.getReasonPhrase(),
                message,
                data,
                LocalDateTime.now(),
                generateTraceId()
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
                LocalDateTime.now(),
                generateTraceId()
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
                LocalDateTime.now(),
                generateTraceId()
        );
    }

    /** Simple error with default BAD_REQUEST status */
    public static <T> TmsApiResponse<T> error(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static <T> TmsApiResponse<T> success(String message, T data) {
        return new TmsApiResponse<>(
                true,
                org.springframework.http.HttpStatus.OK.value(),
                org.springframework.http.HttpStatus.OK.getReasonPhrase(),
                message,
                data,
                LocalDateTime.now(),
                generateTraceId()
        );
    }
    public static <T> TmsApiResponse<T> created(String message, T data) {
        return new TmsApiResponse<>(
                true,
                org.springframework.http.HttpStatus.CREATED.value(),
                org.springframework.http.HttpStatus.CREATED.getReasonPhrase(),
                message,
                data,
                LocalDateTime.now(),
                generateTraceId()
        );
    }

}
