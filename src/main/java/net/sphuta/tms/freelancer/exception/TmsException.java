package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * ==========================================================
 * TmsException
 * ==========================================================
 *
 * Custom domain exception for the TMS (Time Management System).
 *
 * Purpose:
 * - Represents business/domain errors with an associated {@link HttpStatus}.
 * - Used throughout services/controllers to signal error conditions
 *   in a way that maps cleanly to REST API responses.
 *
 * Integration:
 * - Handled globally by {@link GlobalExceptionHandler} to produce
 *   consistent {@link net.sphuta.tms.freelancer.response.TmsApiResponse} JSON.
 */
@Slf4j
public class TmsException extends RuntimeException {

    /** The HTTP status code associated with this exception. */
    @Autowired
    private HttpStatus status;

    // ----------------------- CONSTRUCTORS -----------------------

    /**
     * Constructs a new {@code TmsException}.
     *
     * @param status  HTTP status to associate with this exception
     * @param message detailed error message (safe for API clients)
     */
    public TmsException(HttpStatus status, String message) {
        super(message);
        this.status = status != null ? status : HttpStatus.BAD_REQUEST;
        log.error("Created TmsException with status={} and message='{}'", this.status, message);
    }

    // ----------------------- GETTERS -----------------------

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return HttpStatus
     */
    public HttpStatus getStatus() {
        return status;
    }

    // ----------------------- UTILITY METHODS -----------------------

    /**
     * Converts this exception into a structured {@link TmsApiError}.
     * Useful when you want to manually build an error response
     * (though normally handled by {@link GlobalExceptionHandler}).
     *
     * @param path the request path where the error occurred
     * @return structured {@link TmsApiError} containing details of this exception
     */
    public TmsApiError toApiError(String path) {
        log.error("Converting TmsException (status={}, message='{}') to ApiError for path={}",
                status, getMessage(), path);

        return TmsApiError.builder()
                .error(status.getReasonPhrase())
                .message(getMessage())
                .path(path)
                .status(status.value())
                .build();
    }
}
