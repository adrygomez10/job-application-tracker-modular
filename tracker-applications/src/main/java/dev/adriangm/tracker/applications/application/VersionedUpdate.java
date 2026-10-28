package dev.adriangm.tracker.applications.application;

import dev.adriangm.tracker.applications.domain.JobApplication;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Edits an application from the version the user saw: load it, reject a stale version, let the
 * model decide and save the result in one unit of work. An operation without effect saves nothing.
 */
public final class VersionedUpdate {
    private final ApplicationStore store;
    private final UnitOfWork work;

    public VersionedUpdate(ApplicationStore store, UnitOfWork work)
    {
        this.store = Objects.requireNonNull(store);
        this.work = Objects.requireNonNull(work);
    }

    public JobApplication apply(UUID id, long version, UnaryOperator<JobApplication> change)
    {
        return work.required(() -> {
            var current = store.find(id).orElseThrow(RecordMissing::new);
            if (current.version() != version) {
                throw new EditConflict();
            }
            var changed = change.apply(current);
            return changed.equals(current) ? current : store.save(changed, version);
        });
    }
}
