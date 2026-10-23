package dev.adriangm.tracker.support;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.util.Optional;
import java.util.UUID;

/** Reads from another store and fails every write, as an unavailable database would. */
public final class FailingApplicationStore implements ApplicationStore {
  private final ApplicationStore reads;

  public FailingApplicationStore(ApplicationStore reads) {
    this.reads = reads;
  }

  @Override
  public Optional<JobApplication> find(UUID id) {
    return reads.find(id);
  }

  @Override
  public JobApplication create(JobApplication application) {
    throw new IllegalStateException("Storage unavailable");
  }

  @Override
  public JobApplication save(JobApplication application, long expectedVersion) {
    throw new IllegalStateException("Storage unavailable");
  }
}
