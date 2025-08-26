package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import org.springframework.http.HttpStatus;

/**
 * Custom domain exception for TMS (Time Management System).
 *
 * Purpose:
 * - Represents business/domain errors with an associated HTTP status code.
 * - Provides a utility to convert itself into a standardized {@link TmsDto.ApiError}
 *   for consistent API error responses.
 *
 * Logging:
 * - Logs exception creation with the HTTP status and message.
 */
@Slf4j
public class TmsException extends RuntimeException {

    /** The HTTP status code associated with this exception. */
    private final HttpStatus status;

    // ----------------------- CONSTRUCTORS -----------------------

    /**
     * Constructs a new {@code TmsException}.
     *
     * @param status  HTTP status to associate with this exception
     * @param message detailed error message
     */
    public TmsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        log.warn("Created TmsException with status={} and message='{}'", status, message);
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
     * Converts this exception into an {@link TmsDto.ApiError}.
     *
     * @param path the request path where the error occurred
     * @return structured ApiError containing details of this exception
     */
    public TmsDto.ApiError toApiError(String path) {
        log.debug("Converting TmsException (status={}, message='{}') to ApiError for path={}", status, getMessage(), path);

        return TmsDto.ApiError.builder()
                .error(status.getReasonPhrase())
                .message(getMessage())
                .path(path)
                .status(status.value())
                .build();
    }
}
