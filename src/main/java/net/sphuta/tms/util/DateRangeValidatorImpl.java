package net.sphuta.tms.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.sphuta.tms.dto.ProjectDtos;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean Validation (Jakarta Validation) constraint implementation for {@link DateRangeValidator}.
 *
 * <p>Ensures that, when both dates are present, {@code endDate >= startDate}.
 * If either date is {@code null}, the validator returns {@code true} to allow
 * other validators or business rules to decide (i.e., this is a cross-field
 * constraint that only enforces ordering when both fields exist).</p>
 *
 * <p>Supported targets:</p>
 * <ul>
 *   <li>{@link ProjectDtos.Create}</li>
 *   <li>{@link ProjectDtos.Update}</li>
 * </ul>
 */
public class DateRangeValidatorImpl implements ConstraintValidator<DateRangeValidator, Object> {

    /** Logger for debugging validation outcomes without changing behavior. */
    private static final Logger log = LoggerFactory.getLogger(DateRangeValidatorImpl.class);

    /**
     * Validates that {@code endDate} is not before {@code startDate} when both are present.
     *
     * @param value target object (expected: {@link ProjectDtos.Create} or {@link ProjectDtos.Update})
     * @param ctx   validation context (unused here; message is provided by the annotation)
     * @return {@code true} if valid (or dates missing), {@code false} if endDate &lt; startDate
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        LocalDate start = null;
        LocalDate end = null;

        // Extract dates from supported DTOs. If other types are passed, we do nothing (remain nulls).
        if (value instanceof ProjectDtos.Create c) {
            start = c.getStartDate();
            end   = c.getEndDate();
        } else if (value instanceof ProjectDtos.Update u) {
            start = u.getStartDate();
            end   = u.getEndDate();
        }

        // If either is null, we consider it valid; presence requirements are handled elsewhere.
        if (start == null || end == null) {
            return true;
        }

        // Return true when endDate >= startDate; otherwise log and return false.
        boolean ok = !end.isBefore(start);
        if (!ok) {
            // Debug-level to avoid noisy logs in production. Upgrade to WARN if you want higher visibility.
            log.debug("Date range validation failed: startDate={} endDate={} (endDate is before startDate)", start, end);
        }
        return ok;
    }
}
