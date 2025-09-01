package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * ==========================================================
 * TmsApiResponse
 * ==========================================================
 *
 * Unified API Response wrapper (using record).
 * Provides consistency across success and error cases.
 *
 * @param success    true if request handled successfully
 * @param statusCode numeric HTTP status code
 * @param status     HTTP reason phrase
 * @param message    developer/user-friendly message
 * @param data       response payload (may be null on error)
 * @param timestamp  time response created
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
     * Factory for success response with data.
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
     * Factory for error response (no data).
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
}
