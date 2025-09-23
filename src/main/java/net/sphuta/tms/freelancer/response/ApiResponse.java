package net.sphuta.tms.freelancer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generic API response wrapper used across all endpoints.
 * Ensures consistent structure for success & error responses.
 *
 * @param <T> the type of response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String traceId,
        LocalDateTime timestamp
) {
    /**
     * Derived status string for backward compatibility in tests and clients.
     * Returns "success" if success=true, else "error".
     */
    public String status() {
        return success ? "success" : "error";
    }

    /**
     * Static factory method for success responses.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .traceId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Static factory method for error responses.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .traceId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
