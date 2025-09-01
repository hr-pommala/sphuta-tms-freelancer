package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all endpoints in the Freelancer edition.
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

    /**
     * Factory method for a simple error without extra data.
     */
    public static <T> TmsApiResponse<T> error(HttpStatus status, String message) {
        return new TmsApiResponse<>(
                false,
                status.value(),
                status.getReasonPhrase(),
                message,
                null,
                LocalDateTime.now()
        );
    }
}
