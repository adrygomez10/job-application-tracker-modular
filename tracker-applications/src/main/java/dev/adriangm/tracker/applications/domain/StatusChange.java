package dev.adriangm.tracker.applications.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A requested status. {@code appliedOn} may be empty for saved or withdrawn offers. A correction
 * always needs a reason; the model also treats reopenings and steps back as corrections.
 */
public record StatusChange(Status next, LocalDate appliedOn, String reason, Kind kind) {
    public enum Kind {
        UPDATE,
        CORRECTION
    }

    public StatusChange
    {
        Objects.requireNonNull(next);
        Objects.requireNonNull(kind);
        reason = Checks.optionalText(reason, 10000);
    }

    public static StatusChange update(Status next, LocalDate appliedOn, String reason)
    {
        return new StatusChange(next, appliedOn, reason, Kind.UPDATE);
    }

    public static StatusChange correction(Status next, LocalDate appliedOn, String reason)
    {
        return new StatusChange(next, appliedOn, reason, Kind.CORRECTION);
    }
}
