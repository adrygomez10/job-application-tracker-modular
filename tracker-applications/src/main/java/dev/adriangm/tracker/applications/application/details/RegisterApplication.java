package dev.adriangm.tracker.applications.application.details;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.UnitOfWork;
import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Registers a saved offer or a submitted application. The caller supplies the identity so that a
 * repeated submission of the same form is rejected instead of creating a duplicate.
 */
public final class RegisterApplication {
    private final ApplicationStore store;
    private final UnitOfWork work;
    private final Clock clock;

    public RegisterApplication(ApplicationStore store, UnitOfWork work, Clock clock)
    {
        this.store = Objects.requireNonNull(store);
        this.work = Objects.requireNonNull(work);
        this.clock = Objects.requireNonNull(clock);
    }

    public JobApplication execute(UUID id, ApplicationDetails details, Status status, LocalDate appliedOn)
    {
        return work
            .required(
                () -> store
                    .create(
                        JobApplication.create(id, details, status, appliedOn, clock.instant(), LocalDate.now(clock))
                    )
            );
    }
}
