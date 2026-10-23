package dev.adriangm.tracker.applications.application.activities;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Records whether an interview took place and what came out of it. */
public final class RecordInterviewOutcome {
  private final VersionedUpdate update;
  private final Clock clock;

  public RecordInterviewOutcome(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(
      UUID id, long version, UUID interviewId, Interview.Outcome outcome, String result) {
    return update.apply(
        id,
        version,
        application -> application.finishInterview(interviewId, outcome, result, clock.instant()));
  }
}
