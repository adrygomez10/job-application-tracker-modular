package dev.adriangm.tracker.applications.application.activities;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Completes a follow-up task. Completing it again has no effect. */
public final class CompleteTask {
  private final VersionedUpdate update;
  private final Clock clock;

  public CompleteTask(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version, UUID taskId) {
    return update.apply(
        id, version, application -> application.completeTask(taskId, clock.instant()));
  }
}
