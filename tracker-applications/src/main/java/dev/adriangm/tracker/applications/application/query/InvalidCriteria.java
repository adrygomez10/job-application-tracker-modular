package dev.adriangm.tracker.applications.application.query;

/** A search filter that cannot be interpreted. It is an input error, not a business rule. */
public final class InvalidCriteria extends RuntimeException {
    public InvalidCriteria()
    {
        super("Invalid search criteria.");
    }
}
