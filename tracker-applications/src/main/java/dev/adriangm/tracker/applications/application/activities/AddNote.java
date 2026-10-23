package dev.adriangm.tracker.applications.application.activities;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Adds a free note to the history of an application. */
public final class AddNote {
  private final VersionedUpdate update;
  private final Clock clock;

  public AddNote(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version, String note) {
    return update.apply(id, version, application -> application.addNote(note, clock.instant()));
  }
}
