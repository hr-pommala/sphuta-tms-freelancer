package net.sphuta.tms.exception;

/**
 * Exception type to represent a <strong>conflict</strong> (HTTP 409) scenario.
 *
 * <p>Typical use cases include:</p>
 * <ul>
 *   <li>Uniqueness violations (e.g., project name already exists for a client).</li>
 *   <li>State conflicts (e.g., attempting to archive an already archived project).</li>
 * </ul>
 *
 * <p>This exception is intended to be caught and mapped by a global exception handler
 * (e.g., {@code @ControllerAdvice}) to an HTTP 409 response with a consistent error body.</p>
 */
public class ConflictException extends RuntimeException {

    /**
     * Creates a new ConflictException with a human-readable message.
     *
     * @param msg description of the conflict, suitable for end-user display
     */
    public ConflictException(String msg) {
        super(msg);
    }
}
