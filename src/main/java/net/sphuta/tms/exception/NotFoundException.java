package net.sphuta.tms.exception;

/**
 * Exception type to represent a <strong>resource not found</strong> scenario.
 *
 * <p>Typically mapped to HTTP 404 (Not Found) by the
 * {@link net.sphuta.tms.exception.GlobalExceptionHandler}.</p>
 *
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Project with a given ID does not exist.</li>
 *   <li>Client with a given ID does not exist.</li>
 * </ul>
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new NotFoundException with a descriptive message.
     *
     * @param msg description of the missing resource, suitable for error responses
     */
    public NotFoundException(String msg) {
        super(msg);
    }
}
