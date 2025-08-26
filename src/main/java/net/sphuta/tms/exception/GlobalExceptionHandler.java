package net.sphuta.tms.exception;

import jakarta.validation.ConstraintViolationException;
import net.sphuta.tms.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for REST controllers.
 *
 * <p>Intercepts exceptions thrown in the API layer and converts them into
 * a standardized {@link ApiResponse} error envelope with an appropriate
 * HTTP status code.</p>
 *
 * <p>Benefits:</p>
 * <ul>
 *   <li>Centralized error handling, no need to duplicate in controllers.</li>
 *   <li>Consistent JSON error shape across the API.</li>
 *   <li>Automatic mapping of common exception types (validation, conflicts, not found, etc.).</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Logger for tracing exceptions. */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps {@link NotFoundException} → 404.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> notFound(NotFoundException ex) {
        log.warn("NotFoundException handled: {}", ex.getMessage());
        return wrap(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    /**
     * Maps {@link ConflictException} → 409.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<?>> conflict(ConflictException ex) {
        log.warn("ConflictException handled: {}", ex.getMessage());
        return wrap(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null);
    }

    /**
     * Maps {@link MethodArgumentNotValidException} (bean validation on @RequestBody) → 400.
     * Collects field-level errors into a map for the response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> beanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errs.put(fe.getField(), fe.getDefaultMessage()));

        log.debug("Validation errors: {}", errs);
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed", "Check validationErrors", errs);
    }

    /**
     * Maps {@link ConstraintViolationException} (query/path param validation) → 400.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> pathQueryValidation(ConstraintViolationException ex) {
        log.debug("ConstraintViolationException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), null);
    }

    /**
     * Maps {@link DataIntegrityViolationException} (DB-level constraint failures) → 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> dbConflicts(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException handled: {}", ex.getMostSpecificCause().getMessage());
        return wrap(HttpStatus.CONFLICT, "Conflict", "Unique or FK constraint violated", null);
    }

    /**
     * Maps {@link IllegalArgumentException} → 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> badRequest(IllegalArgumentException ex) {
        log.debug("IllegalArgumentException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    /**
     * Catch-all: any unhandled exception → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> unknown(Exception ex) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return wrap(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", null);
    }

    /**
     * Utility method to build a standardized {@link ApiResponse} error wrapper
     * and return it in a {@link ResponseEntity} with the proper HTTP status.
     */
    private ResponseEntity<ApiResponse<?>> wrap(HttpStatus status, String error, String message, Object details) {
        var err = new ApiResponse.ApiError(status.value(), error, message, details);
        return ResponseEntity.status(status).body(ApiResponse.fail(err));
    }
}
