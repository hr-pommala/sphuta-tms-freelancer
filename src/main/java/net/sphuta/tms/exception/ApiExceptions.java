package net.sphuta.tms.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application-specific exceptions used to map to HTTP responses.
 * <p>
 * Each nested static class represents a particular category of exception
 * (e.g., NotFound, Conflict, Validation, Forbidden, Unauthorized).
 * These exceptions are thrown within the service/controller layers and
 * handled globally by {@link GlobalExceptionHandler}.
 */
public class ApiExceptions {

    /** Logger instance for capturing exception creation logs. */
    private static final Logger log = LoggerFactory.getLogger(ApiExceptions.class);

    /**
     * Exception thrown when a requested resource is not found.
     */
    public static class NotFoundException extends RuntimeException {
        /**
         * Constructor accepting a message.
         * @param m the error message
         */
        public NotFoundException(String m) {
            super(m);
            // Log at error level when this exception is constructed
            log.error("NotFoundException created with message: {}", m);
        }
    }

    /**
     * Exception thrown when a conflict occurs (e.g., duplicate resource, invalid state).
     */
    public static class ConflictException extends RuntimeException {
        /**
         * Constructor accepting a message.
         * @param m the error message
         */
        public ConflictException(String m) {
            super(m);
            // Log at error level when this exception is constructed
            log.error("ConflictException created with message: {}", m);
        }
    }

    /**
     * Exception thrown when validation fails (e.g., invalid input).
     */
    public static class ValidationException extends RuntimeException {
        /**
         * Constructor accepting a message.
         * @param m the error message
         */
        public ValidationException(String m) {
            super(m);
            // Log at error level when this exception is constructed
            log.error("ValidationException created with message: {}", m);
        }
    }

    /**
     * Exception thrown when access is forbidden (user lacks privileges).
     */
    public static class ForbiddenException extends RuntimeException {
        /**
         * Constructor accepting a message.
         * @param m the error message
         */
        public ForbiddenException(String m) {
            super(m);
            // Log at error level when this exception is constructed
            log.error("ForbiddenException created with message: {}", m);
        }
    }

    /**
     * Exception thrown when authentication fails (invalid/missing credentials).
     */
    public static class UnauthorizedException extends RuntimeException {
        /**
         * Constructor accepting a message.
         * @param m the error message
         */
        public UnauthorizedException(String m) {
            super(m);
            // Log at error level when this exception is constructed
            log.error("UnauthorizedException created with message: {}", m);
        }
    }
}
