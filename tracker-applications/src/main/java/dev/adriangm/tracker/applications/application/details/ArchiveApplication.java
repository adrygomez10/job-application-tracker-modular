package dev.adriangm.tracker.applications.application.details;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Hides an application from summaries and agenda while keeping all its information. */
public final class ArchiveApplication {
  private final VersionedUpdate update;
  private final Clock clock;

  public ArchiveApplication(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version) {
    return update.apply(id, version, application -> application.archive(clock.instant()));
  }
}
