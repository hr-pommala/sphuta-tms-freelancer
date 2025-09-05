package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ===============================================================
 * GlobalExceptionHandler
 * ===============================================================
 *
 * <p>This class is a central point for handling all exceptions
 * thrown across the application. It ensures consistent error
 * responses to the client in a structured {@link TmsApiResponse} format.</p>
 *
 * <p><b>Features:</b></p>
 * - Handles both custom exceptions (e.g., {@code NotFoundException}) and Spring/Java built-ins. <br>
 * - Returns meaningful HTTP status codes aligned with REST best practices. <br>
 * - Logs errors for debugging while hiding sensitive details from clients. <br>
 * - Converts validation and database errors into user-friendly messages. <br>
 *
 * <p>Annotations used:</p>
 * - {@link RestControllerAdvice} → ensures JSON response bodies. <br>
 * - {@link ControllerAdvice} → allows cross-controller exception handling. <br>
 */
@Slf4j
@RestControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------------
    // CUSTOM EXCEPTION HANDLERS
    // ------------------------------------------------------------------------

    /**
     * Handles resource-not-found scenarios.
     *
     * @param ex thrown when a requested resource does not exist
     * @return {@link ResponseEntity} with 404 status and error message
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<TmsApiResponse<?>> notFound(NotFoundException ex) {
        log.error("NotFoundException handled: {}", ex.getMessage());
        return wrap(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Handles conflict scenarios such as duplicate entries.
     *
     * @param ex thrown when data conflicts with existing state
     * @return {@link ResponseEntity} with 409 status and error message
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<TmsApiResponse<?>> conflict(ConflictException ex) {
        log.error("ConflictException handled: {}", ex.getMessage());
        return wrap(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Handles generic custom {@link TmsException}.
     *
     * @param ex   thrown for application-specific business logic errors
     * @param req  HTTP request that caused the error
     * @return standardized error response with custom or default status
     */
    @ExceptionHandler(TmsException.class)
    public ResponseEntity<TmsApiResponse<Void>> handleTms(TmsException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        log.error("TmsException {} on {}: {}", status.value(), req.getRequestURI(), ex.getMessage());

        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    // ------------------------------------------------------------------------
    // VALIDATION HANDLERS
    // ------------------------------------------------------------------------

    /**
     * Handles request body validation failures (e.g., @NotNull, @Email).
     *
     * @param ex   contains binding/validation errors
     * @param req  request URI where validation failed
     * @return structured response with field-level error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        // Collect validation errors into a field → message map
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> (fe.getDefaultMessage() == null || fe.getDefaultMessage().isBlank())
                                ? "Invalid or required field"
                                : fe.getDefaultMessage(),
                        (a, b) -> a + ", " + b, // merge multiple errors for same field
                        LinkedHashMap::new
                ));

        log.warn("400 Validation failed on {}: {}", req.getRequestURI(), details);

        TmsApiResponse<Map<String, String>> body = new TmsApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed: required/invalid fields present",
                details,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles validation failures in query/path params (e.g., @Min, @Pattern).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> pathQueryValidation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage(), null);
    }

    /**
     * Handles database integrity violations (e.g., duplicate key, FK constraint).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> dbConflicts(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException handled: {}", ex.getMostSpecificCause().getMessage());
        return wrap(HttpStatus.CONFLICT, "Unique or FK constraint violated", null);
    }

    /**
     * Handles illegal arguments passed to APIs or services.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TmsApiResponse<?>> badRequest(IllegalArgumentException ex) {
        log.error("IllegalArgumentException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Fallback handler for all uncaught/unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsApiResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);

        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected error",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ------------------------------------------------------------------------
    // UTILITY METHODS
    // ------------------------------------------------------------------------

    /**
     * Helper to wrap error details in a {@link TmsApiResponse}.
     *
     * @param status  HTTP status to return
     * @param message human-readable error message
     * @param details optional map/object with error details
     * @return standardized {@link ResponseEntity} with error body
     */
    private ResponseEntity<TmsApiResponse<?>> wrap(HttpStatus status, String message, Object details) {
        var body = TmsApiResponse.failure(status, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
