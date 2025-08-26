package net.sphuta.tms.freelancer.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the TMS application.
 *
 * Purpose:
 * - Provides centralized error handling across all REST controllers.
 * - Ensures consistent JSON error responses for client applications.
 * - Logs exceptions with different levels (warn/error) depending on severity.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------- CUSTOM EXCEPTION HANDLER -----------------------

    /**
     * Handles custom {@link TmsException}.
     *
     * Behavior:
     * - Logs a warning with status code, request path, and message.
     * - Converts the exception into a standardized {@link TmsDto.ApiError}.
     *
     * @param ex   the thrown TmsException
     * @param req  current HTTP request
     * @return ResponseEntity with error body and status
     */
    @ExceptionHandler(TmsException.class)
    public ResponseEntity<TmsDto.ApiError> handleTms(TmsException ex, HttpServletRequest req) {
        log.warn("TmsException {} on {}: {}", ex.getStatus().value(), req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(withTs(ex.toApiError(req.getRequestURI())), ex.getStatus());
    }

    // ----------------------- VALIDATION ERROR HANDLER -----------------------

    /**
     * Handles validation errors thrown by {@link MethodArgumentNotValidException}.
     *
     * Behavior:
     * - Collects all field validation errors into a key-value map.
     * - Logs a warning with the request path and details.
     * - Returns a BAD_REQUEST (400) response with structured error information.
     *
     * @param ex   the validation exception
     * @param req  current HTTP request
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsDto.ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a + ", " + b // merge messages if multiple errors on the same field
                ));
        log.warn("400 Validation failed on {}: {}", req.getRequestURI(), details);

        TmsDto.ApiError body = TmsDto.ApiError.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed: " + details)
                .path(req.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.badRequest().body(withTs(body));
    }

    // ----------------------- FALLBACK EXCEPTION HANDLER -----------------------

    /**
     * Handles any other unhandled {@link Exception}.
     *
     * Behavior:
     * - Logs the error stack trace with severity level ERROR.
     * - Returns a generic INTERNAL_SERVER_ERROR (500) response.
     *
     * @param ex   the thrown exception
     * @param req  current HTTP request
     * @return ResponseEntity with error details and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsDto.ApiError> handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);

        TmsDto.ApiError body = TmsDto.ApiError.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return new ResponseEntity<>(withTs(body), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ----------------------- HELPER METHOD -----------------------

    /**
     * Utility method to add a timestamp to {@link TmsDto.ApiError}.
     *
     * @param e  the ApiError instance
     * @return ApiError with current timestamp added
     */
    private TmsDto.ApiError withTs(TmsDto.ApiError e) {
        return TmsDto.ApiError.builder()
                .error(e.error())
                .message(e.message())
                .path(e.path())
                .status(e.status())
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
