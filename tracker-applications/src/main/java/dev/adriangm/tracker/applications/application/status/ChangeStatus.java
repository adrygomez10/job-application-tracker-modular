package dev.adriangm.tracker.applications.application.status;

import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.StatusChange;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Changes the status of an application from the version the user edited. The model decides whether
 * the change needs a reason; this class only coordinates loading, deciding and saving.
 */
public final class ChangeStatus {
    private final VersionedUpdate update;
    private final Clock clock;

    public ChangeStatus(VersionedUpdate update, Clock clock)
    {
        this.update = Objects.requireNonNull(update);
        this.clock = Objects.requireNonNull(clock);
    }

    public JobApplication execute(UUID id, long version, StatusChange change)
    {
        return update
            .apply(id, version, application -> application.changeStatus(change, clock.instant(), LocalDate.now(clock)));
    }
}
