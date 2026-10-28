package dev.adriangm.tracker.applications.application.activities;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Schedules an interview or technical test. New interviews always start as scheduled. */
public final class ScheduleInterview {
    private final VersionedUpdate update;
    private final Clock clock;

    public ScheduleInterview(VersionedUpdate update, Clock clock)
    {
        this.update = Objects.requireNonNull(update);
        this.clock = Objects.requireNonNull(clock);
    }

    public JobApplication execute(UUID id, long version, Request request)
    {
        var interview = Interview
            .schedule(
                UUID.randomUUID(),
                request.title(),
                request.kind(),
                request.startsAt(),
                request.location(),
                request.url(),
                request.notes()
            );
        return update.apply(id, version, application -> application.addInterview(interview, clock.instant()));
    }

    /** Data the user provides; validation belongs to {@link Interview}. */
    public record Request(
        String title,
        Interview.Kind kind,
        Instant startsAt,
        String location,
        String url,
        String notes
    ) {
    }
}
