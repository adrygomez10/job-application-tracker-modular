package dev.adriangm.tracker.applications.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Interview(
    UUID id,
    String title,
    Kind kind,
    Instant startsAt,
    String location,
    String url,
    String notes,
    Outcome outcome,
    String result
) {
    public enum Kind {
        INTERVIEW,
        TECHNICAL_TEST,
        OTHER
    }

    public enum Outcome {
        SCHEDULED,
        COMPLETED,
        CANCELLED
    }

    public Interview
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(startsAt);
        Objects.requireNonNull(outcome);
        title = Checks.requiredText(title, 180);
        location = Checks.optionalText(location, 500);
        url = Checks.url(url);
        notes = Checks.optionalText(notes, 10000);
        result = Checks.optionalText(result, 10000);
    }

    public static Interview schedule(
        UUID id,
        String title,
        Kind kind,
        Instant startsAt,
        String location,
        String url,
        String notes
    )
    {
        return new Interview(id, title, kind, startsAt, location, url, notes, Outcome.SCHEDULED, "");
    }

    Interview finish(Outcome next, String nextResult)
    {
        if (next == Outcome.SCHEDULED) {
            throw new RuleViolation("interviewOutcome");
        }
        return new Interview(id, title, kind, startsAt, location, url, notes, next, nextResult);
    }
}
