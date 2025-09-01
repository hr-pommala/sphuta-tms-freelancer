package net.sphuta.tms.freelancer.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ==========================================================
 * GlobalExceptionHandler
 * ==========================================================
 *
 * Centralized exception handling for the TMS application.
 *
 * Purpose:
 * - Ensures consistent JSON error responses across all controllers.
 * - Wraps error details inside {@link TmsApiResponse} for uniformity.
 * - Provides clear logs for debugging and production monitoring.
 *
 * Error Handling Rules:
 * - {@link TmsException}: Custom application exception → response status from exception.
 * - {@link MethodArgumentNotValidException}: Bean validation failure → 400, with per-field messages in "data".
 * - {@link Exception}: Any unhandled exception → 500, with a generic safe message.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------- CUSTOM EXCEPTION HANDLER -----------------------

    /**
     * Handles custom {@link TmsException}.
     *
     * @param ex  thrown business exception
     * @param req the current HTTP request
     * @return ResponseEntity with {@link TmsApiResponse} (status, message, timestamp)
     */
    @ExceptionHandler(TmsException.class)
    public ResponseEntity<TmsApiResponse<Void>> handleTms(TmsException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        log.error("TmsException {} on {}: {}", status.value(), req.getRequestURI(), ex.getMessage());

        // Build unified API error response
        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,                          // success flag
                status.value(),                 // HTTP code
                status.getReasonPhrase(),       // HTTP reason (e.g., "Bad Request")
                ex.getMessage(),                // developer-friendly message
                null,                           // no additional data payload
                LocalDateTime.now()             // current timestamp
        );
        return ResponseEntity.status(status).body(body);
    }

    // ----------------------- VALIDATION ERROR HANDLER -----------------------

    /**
     * Handles validation errors (JSR-303 annotations) when request DTOs fail constraints.
     * Collects field-level error messages into a map and returns 400 Bad Request.
     *
     * @param ex  validation exception with field errors
     * @param req current HTTP request
     * @return ResponseEntity with error details per field inside {@link TmsApiResponse#}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex,
                                                                                HttpServletRequest req) {

        // Collect field errors into an ordered map; merge duplicate field errors into comma-separated string
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> (fe.getDefaultMessage() == null || fe.getDefaultMessage().isBlank())
                                ? "Invalid or required field"
                                : fe.getDefaultMessage(),
                        (a, b) -> a + ", " + b,   // merge messages if multiple errors on same field
                        LinkedHashMap::new
                ));

        log.warn("400 Validation failed on {}: {}", req.getRequestURI(), details);

        // Wrap error in API response format
        TmsApiResponse<Map<String, String>> body = new TmsApiResponse<>(
                false,                                      // success flag
                HttpStatus.BAD_REQUEST.value(),             // 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(),   // "Bad Request"
                "Validation failed: required/invalid fields present",
                details,                                    // field errors under "data"
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ----------------------- FALLBACK EXCEPTION HANDLER -----------------------

    /**
     * Handles any other unhandled {@link Exception}.
     * Logs stack trace and returns 500 Internal Server Error with a safe, generic message.
     *
     * @param ex  uncaught exception
     * @param req current HTTP request
     * @return ResponseEntity with {@link TmsApiResponse} generic error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsApiResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);

        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,                                     // success flag
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), // "Internal Server Error"
                "Unexpected error",                        // keep generic for client safety
                null,                                      // no data
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
