package net.sphuta.tms.freelancer.exception;

import jakarta.validation.ConstraintViolationException;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for REST controllers.
 *
 * <p>Intercepts exceptions thrown in the API layer and converts them into
 * a standardized {@link TmsApiResponse} error envelope with an appropriate
 * HTTP status code.</p>
 *
 * <p>Benefits:</p>
 * <ul>
 *   <li>Centralized error handling, no need to duplicate in controllers.</li>
 *   <li>Consistent JSON error shape across the API.</li>
 *   <li>Automatic mapping of common exception types (validation, conflicts, not found, etc.).</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps {@link NotFoundException} → 404.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<TmsApiResponse<?>> notFound(NotFoundException ex) {
        log.error("NotFoundException handled: {}", ex.getMessage());
        return wrap(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    /**
     * Maps {@link ConflictException} → 409.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<TmsApiResponse<?>> conflict(ConflictException ex) {
        log.error("ConflictException handled: {}", ex.getMessage());
        return wrap(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null);
    }

    /**
     * Maps {@link MethodArgumentNotValidException} (bean validation on @RequestBody) → 400.
     * Collects field-level errors into a map for the response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsApiResponse<?>> beanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errs.put(fe.getField(), fe.getDefaultMessage()));

        log.error("Validation errors: {}", errs);
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed", "Check validationErrors", errs);
    }

    /**
     * Maps {@link ConstraintViolationException} (query/path param validation) → 400.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> pathQueryValidation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), null);
    }

    /**
     * Maps {@link DataIntegrityViolationException} (DB-level constraint failures) → 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> dbConflicts(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException handled: {}", ex.getMostSpecificCause().getMessage());
        return wrap(HttpStatus.CONFLICT, "Conflict", "Unique or FK constraint violated", null);
    }

    /**
     * Maps {@link IllegalArgumentException} → 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TmsApiResponse<?>> badRequest(IllegalArgumentException ex) {
        log.error("IllegalArgumentException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    /**
     * Catch-all: any unhandled exception → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsApiResponse<?>> unknown(Exception ex) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return wrap(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", null);
    }

    /**
     * Utility method to build a standardized {@link TmsApiResponse} error wrapper
     * and return it in a {@link ResponseEntity} with the proper HTTP status.
     *
     * <p>Note: With the new TmsApiResponse shape, validation/extra details are placed in the
     * {@code data} field for error responses, per your requirement.</p>
     */
    private ResponseEntity<TmsApiResponse<?>> wrap(HttpStatus status, String error, String message, Object details) {
        // 'error' is retained to avoid losing any information, but the response schema already
        // includes the HTTP reason phrase via TmsApiResponse; we pass 'message' and 'details' through.
        var body = TmsApiResponse.failure(status, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
