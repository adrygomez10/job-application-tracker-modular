package dev.adriangm.tracker.applications.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record FollowUpTask(UUID id, String title, LocalDate dueOn, boolean completed) {
  public FollowUpTask {
    Objects.requireNonNull(id);
    title = Checks.requiredText(title, 180);
    Objects.requireNonNull(dueOn);
  }

  public static FollowUpTask pending(UUID id, String title, LocalDate dueOn) {
    return new FollowUpTask(id, title, dueOn, false);
  }

  FollowUpTask complete() {
    return new FollowUpTask(id, title, dueOn, true);
  }
}
