package dev.adriangm.tracker.applications.application.activities;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.FollowUpTask;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Adds a pending follow-up task with a due date. */
public final class AddTask {
  private final VersionedUpdate update;
  private final Clock clock;

  public AddTask(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version, String title, LocalDate dueOn) {
    var task = FollowUpTask.pending(UUID.randomUUID(), title, dueOn);
    return update.apply(id, version, application -> application.addTask(task, clock.instant()));
  }
}
