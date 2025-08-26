package net.sphuta.tms.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure that a DTO/entity
 * has a valid date range where {@code endDate >= startDate}.
 *
 * <p>Intended to be applied at the class level, for types that
 * have both {@code startDate} and {@code endDate} fields.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * &#64;DateRangeValidator
 * public class ProjectDtos.Create {
 *     private LocalDate startDate;
 *     private LocalDate endDate;
 * }
 * </pre>
 *
 * <p>Implementation is handled by {@link DateRangeValidatorImpl}.</p>
 */
@Target({ElementType.TYPE})           // apply to class-level
@Retention(RetentionPolicy.RUNTIME)   // available at runtime
@Documented                           // include in Javadoc
@Constraint(validatedBy = DateRangeValidatorImpl.class)
public @interface DateRangeValidator {

    /**
     * Default validation message when constraint is violated.
     */
    String message() default "endDate must be on/after startDate";

    /**
     * Allows grouping of constraints.
     */
    Class<?>[] groups() default {};

    /**
     * Can carry metadata information about severity/payload.
     */
    Class<? extends Payload>[] payload() default {};
}
