package dev.adriangm.tracker.applications.application.details;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Updates the descriptive data of an application without touching its status. */
public final class EditDetails {
  private final VersionedUpdate update;
  private final Clock clock;

  public EditDetails(VersionedUpdate update, Clock clock) {
    this.update = Objects.requireNonNull(update);
    this.clock = Objects.requireNonNull(clock);
  }

  public JobApplication execute(UUID id, long version, ApplicationDetails details) {
    return update.apply(id, version, application -> application.edit(details, clock.instant()));
  }
}
