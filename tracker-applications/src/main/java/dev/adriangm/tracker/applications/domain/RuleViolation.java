package dev.adriangm.tracker.applications.domain;

public final class RuleViolation extends RuntimeException {
    private final String code;

    public RuleViolation(String code)
    {
        this(code, null);
    }

    public RuleViolation(String code, Throwable cause)
    {
        super("Business rule violated: " + code, cause);
        this.code = code;
    }

    public String code()
    {
        return code;
    }
}
