package dev.adriangm.tracker.applications.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TimelineEntry(
    UUID id,
    Instant occurredAt,
    TimelineEvent kind,
    Status previousStatus,
    Status nextStatus,
    String body
) {
    public TimelineEntry
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(kind);
        body = Checks.optionalText(body, 10000);
    }

    static TimelineEntry of(Instant now, TimelineEvent kind, String body)
    {
        return new TimelineEntry(UUID.randomUUID(), now, kind, null, null, body);
    }

    static TimelineEntry statusChange(Instant now, TimelineEvent kind, Status previous, Status next, String reason)
    {
        return new TimelineEntry(UUID.randomUUID(), now, kind, previous, next, reason);
    }
}
