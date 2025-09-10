package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized custom API exceptions for the TMS application.
 *
 * <p>This class groups all common exceptions used across the system,
 * ensuring consistent error handling and logging behavior.
 *
 * Each exception type extends {@link RuntimeException} so that they
 * can propagate unchecked and be handled globally (e.g., by a
 * {@code @ControllerAdvice}).
 *
 * <p>Lombok's {@link Slf4j} annotation provides a logger instance
 * named 'log' for logging exception messages when they are created.
 */
@Slf4j
public class ApiExceptions {

    private ApiExceptions() { /* utility */ }
    /**
     * Exception thrown when a requested resource is not found.
     *
     * <p>Example: A timesheet with a given ID does not exist.
     */
    public static class NotFoundException extends RuntimeException {
        /**
         * Constructor with message.
         *
         * @param m Error message describing what was not found
         */
        public NotFoundException(String m) {
            super(m); // Call parent RuntimeException constructor
            log.error("NotFound: {}", m); // Log error with message
        }
    }

    /**
     * Exception thrown when there is a conflict with the current state.
     *
     * <p>Example: Attempting to create a timesheet that overlaps
     * with an existing one for the same project.
     */
    public static class ConflictException extends RuntimeException {
        /**
         * Constructor with message.
         *
         * @param m Error message describing the conflict
         */
        public ConflictException(String m) {
            super(m); // Call parent RuntimeException constructor
            log.error("Conflict: {}", m); // Log error with message
        }
    }

    /**
     * Exception thrown when validation of request data fails.
     *
     * <p>Example: Invalid dates or negative hours submitted in a request.
     */
    public static class ValidationException extends RuntimeException {
        /**
         * Constructor with message.
         *
         * @param m Error message describing the validation failure
         */
        public ValidationException(String m) {
            super(m); // Call parent RuntimeException constructor
            log.error("Validation: {}", m); // Log error with message
        }
    }

    /**
     * Exception thrown when a user tries to access a resource
     * they do not have permission to access.
     *
     * <p>Example: Trying to approve a timesheet without manager role.
     */
    public static class ForbiddenException extends RuntimeException {
        /**
         * Constructor with message.
         *
         * @param m Error message describing the forbidden access
         */
        public ForbiddenException(String m) {
            super(m); // Call parent RuntimeException constructor
            log.error("Forbidden: {}", m); // Log error with message
        }
    }

    /**
     * Exception thrown when authentication fails or
     * user credentials are invalid/missing.
     *
     * <p>Example: Accessing an API endpoint without a valid JWT token.
     */
    public static class UnauthorizedException extends RuntimeException {
        /**
         * Constructor with message.
         *
         * @param m Error message describing the unauthorized access
         */
        public UnauthorizedException(String m) {
            super(m); // Call parent RuntimeException constructor
            log.error("Unauthorized: {}", m); // Log error with message
        }
    }

    /**
     * Generic unchecked API exception used in service layer.
     * Add fields (status, code) later if needed.
     */
    public static class ApiException extends RuntimeException {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}