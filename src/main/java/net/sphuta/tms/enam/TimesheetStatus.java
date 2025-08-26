package net.sphuta.tms.enam;


/** Status enum with a Java 17 switch expression for mutability. */
public enum TimesheetStatus {
    DRAFT, APPROVED, LOCKED;

    public boolean isMutable() {
        return switch (this) {
            case DRAFT, APPROVED -> true;
            case LOCKED -> false;
        };
    }
}
