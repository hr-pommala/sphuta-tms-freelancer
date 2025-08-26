package net.sphuta.tms.freelancer.exception;

import net.sphuta.tms.freelancer.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * -----------------------
 * Centralized exception handler for the entire application.
 * Converts application-specific and framework exceptions into
 * standardized HTTP responses with {@link ApiResponse}.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Logger instance for capturing exception details. */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors thrown when method arguments fail @Valid checks.
     *
     * @param ex the validation exception
     * @return ResponseEntity with status 400 and validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        // Collect validation errors from binding result
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        // Log warning and error for diagnostics
        log.warn("Validation failed: {}", errors);
        log.error("ValidationException encountered", ex);
        // Build and return a bad request response
        return ResponseEntity.badRequest().body(ApiResponse.fail("Validation failed: " + errors));
    }

    /**
     * Handles resource not found exceptions.
     *
     * @param ex NotFoundException
     * @return ResponseEntity with 404 status and error message
     */
    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    public ResponseEntity<?> handleNotFound(ApiExceptions.NotFoundException ex) {
        // Log error before returning
        log.error("NotFoundException encountered", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles conflict exceptions (e.g., duplicate resources).
     *
     * @param ex ConflictException
     * @return ResponseEntity with 409 status and error message
     */
    @ExceptionHandler(ApiExceptions.ConflictException.class)
    public ResponseEntity<?> handleConflict(ApiExceptions.ConflictException ex) {
        // Log error before returning
        log.error("ConflictException encountered", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles forbidden access exceptions.
     *
     * @param ex ForbiddenException
     * @return ResponseEntity with 403 status and error message
     */
    @ExceptionHandler(ApiExceptions.ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ApiExceptions.ForbiddenException ex) {
        // Log error before returning
        log.error("ForbiddenException encountered", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles unauthorized access exceptions.
     *
     * @param ex UnauthorizedException
     * @return ResponseEntity with 401 status and error message
     */
    @ExceptionHandler(ApiExceptions.UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(ApiExceptions.UnauthorizedException ex) {
        // Log error before returning
        log.error("UnauthorizedException encountered", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex generic Exception
     * @return ResponseEntity with 500 status and generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        // Log unexpected error with stack trace
        log.error("Unexpected error", ex);
        // Build and return an internal server error response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Internal Server Error"));
    }
}
