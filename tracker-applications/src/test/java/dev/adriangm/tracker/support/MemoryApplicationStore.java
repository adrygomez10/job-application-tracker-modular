package dev.adriangm.tracker.support;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.EditConflict;
import dev.adriangm.tracker.applications.application.RecordMissing;
import dev.adriangm.tracker.applications.domain.JobApplication;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Test adapter. Immutable snapshots prevent accidental persistence through mutation. */
public final class MemoryApplicationStore implements ApplicationStore {
  private final Map<UUID, JobApplication> records = new HashMap<>();

  @Override
  public synchronized Optional<JobApplication> find(UUID id) {
    return Optional.ofNullable(records.get(id));
  }

  @Override
  public synchronized JobApplication create(JobApplication application) {
    if (records.containsKey(application.id())) {
      throw new EditConflict();
    }
    var saved = application.withVersion(0);
    records.put(saved.id(), saved);
    return saved;
  }

  @Override
  public synchronized JobApplication save(JobApplication application, long expectedVersion) {
    var current = find(application.id()).orElseThrow(RecordMissing::new);
    if (current.version() != expectedVersion) {
      throw new EditConflict();
    }
    var saved = application.withVersion(expectedVersion + 1);
    records.put(saved.id(), saved);
    return saved;
  }
}
