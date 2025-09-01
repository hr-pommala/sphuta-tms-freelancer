package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
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
 *
 * <p>This class provides a centralized exception handling mechanism for the application.
 * It catches exceptions thrown across controllers and returns structured {@link TmsApiResponse}
 * objects with proper HTTP status codes and messages.
 *
 * <p>It uses Spring's {@link ControllerAdvice} and {@link ExceptionHandler} to map specific
 * exceptions to corresponding response formats.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors triggered by {@link MethodArgumentNotValidException}.
     * <p>Extracts all field errors and returns them in a structured map response.
     *
     * @param ex the validation exception containing field-level errors
     * @return ResponseEntity with {@link TmsApiResponse} containing validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        // Collect field errors into a map: fieldName -> errorMessage
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(TmsApiResponse.validationError(errors));
    }

    /**
     * Handles {@link ApiExceptions.NotFoundException}.
     * <p>Used when a requested resource cannot be found.
     *
     * @param ex the custom NotFoundException
     * @return ResponseEntity with {@link TmsApiResponse} and HTTP 404 status
     */
    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    public ResponseEntity<TmsApiResponse<Object>> handleNotFound(ApiExceptions.NotFoundException ex) {
        log.error("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TmsApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * Handles {@link ApiExceptions.ConflictException}.
     * <p>Used when a request causes a conflict, e.g., duplicate resource.
     *
     * @param ex the custom ConflictException
     * @return ResponseEntity with {@link TmsApiResponse} and HTTP 409 status
     */
    @ExceptionHandler(ApiExceptions.ConflictException.class)
    public ResponseEntity<TmsApiResponse<Object>> handleConflict(ApiExceptions.ConflictException ex) {
        log.error("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(TmsApiResponse.error(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * Handles {@link ApiExceptions.ForbiddenException}.
     * <p>Used when access is denied to a resource.
     *
     * @param ex the custom ForbiddenException
     * @return ResponseEntity with {@link TmsApiResponse} and HTTP 403 status
     */
    @ExceptionHandler(ApiExceptions.ForbiddenException.class)
    public ResponseEntity<TmsApiResponse<Object>> handleForbidden(ApiExceptions.ForbiddenException ex) {
        log.error("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TmsApiResponse.error(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    /**
     * Handles {@link ApiExceptions.UnauthorizedException}.
     * <p>Used when authentication fails or user is not authorized.
     *
     * @param ex the custom UnauthorizedException
     * @return ResponseEntity with {@link TmsApiResponse} and HTTP 401 status
     */
    @ExceptionHandler(ApiExceptions.UnauthorizedException.class)
    public ResponseEntity<TmsApiResponse<Object>> handleUnauthorized(ApiExceptions.UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TmsApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * Handles any other unexpected exceptions not explicitly mapped.
     *
     * @param ex the unexpected exception
     * @return ResponseEntity with {@link TmsApiResponse} and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsApiResponse<Object>> handleOther(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(TmsApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
    }
}
