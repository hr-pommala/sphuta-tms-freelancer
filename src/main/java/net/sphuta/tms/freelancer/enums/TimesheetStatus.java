package net.sphuta.tms.freelancer.enums;

/**
 * ==========================================================
 * {@code TimesheetStatus}
 * ==========================================================
 *
 * <p>Enumeration that represents the lifecycle status of a timesheet.</p>
 *
 * <p><b>Defined States:</b></p>
 * - {@link #DRAFT} → Initial state, editable by the user. <br>
 * - {@link #APPROVED} → Reviewed/approved, but still mutable (e.g., before locking). <br>
 * - {@link #LOCKED} → Finalized, cannot be modified. <br>
 *
 * <p><b>Design Highlights:</b></p>
 * - Uses a Java 17 {@code switch} expression in {@link #isMutable()} for clarity and immutability. <br>
 * - Provides domain-driven rules (mutability) inside the enum instead of spreading logic across services. <br>
 */
public enum TimesheetStatus {

    // ------------------------------------------------------------------------
    // ENUM CONSTANTS
    // ------------------------------------------------------------------------

    /** Timesheet is in draft mode; fully editable. */
    DRAFT,

    /** Timesheet has been approved; still editable until locked. */
    APPROVED,

    /** Timesheet is locked; no further changes allowed. */
    LOCKED;

    // ------------------------------------------------------------------------
    // BUSINESS LOGIC
    // ------------------------------------------------------------------------

    /**
     * Determines if a timesheet in the given status is still mutable.
     *
     * @return {@code true} if the timesheet can be edited,
     *         {@code false} if it is locked.
     *
     * <p><b>Implementation detail:</b></p>
     * Uses a Java 17 {@code switch} expression for conciseness and exhaustiveness check.
     */
    public boolean isMutable() {
        return switch (this) {
            case DRAFT, APPROVED -> true;
            case LOCKED -> false;
        };
    }
}
