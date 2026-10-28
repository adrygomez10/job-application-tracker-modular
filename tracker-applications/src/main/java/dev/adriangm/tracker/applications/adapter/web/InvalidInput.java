package dev.adriangm.tracker.applications.adapter.web;

/** A value the web entry cannot interpret, reported with a message code. */
public final class InvalidInput extends RuntimeException {
    private final String code;

    public InvalidInput(String code)
    {
        super("Invalid input: " + code);
        this.code = code;
    }

    public String code()
    {
        return code;
    }
}
