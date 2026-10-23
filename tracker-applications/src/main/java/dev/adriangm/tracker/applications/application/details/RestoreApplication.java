package dev.adriangm.tracker.applications.application.details;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Brings an archived application back to summaries and agenda. */
public final class RestoreApplication {
  private final VersionedUpdate update;
  private final Clock clock;

  public RestoreApplication(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version) {
    return update.apply(id, version, application -> application.restore(clock.instant()));
  }
}
