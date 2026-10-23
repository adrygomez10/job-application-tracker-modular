package dev.adriangm.tracker.applications.application;

import dev.adriangm.tracker.applications.domain.JobApplication;
import java.util.Optional;
import java.util.UUID;

/**
 * Invoke within UnitOfWork. Saves are atomic and reject stale versions; absence never returns null.
 */
public interface ApplicationStore {
  Optional<JobApplication> find(UUID id);

  JobApplication create(JobApplication application);

  JobApplication save(JobApplication application, long expectedVersion);
}
