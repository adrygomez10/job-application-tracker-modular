package dev.adriangm.tracker.applications.adapter.persistence;

import dev.adriangm.tracker.applications.domain.FollowUpTask;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
class TaskRow {
  UUID id;
  String title;
  LocalDate dueOn;
  boolean completed;

  protected TaskRow() {}

  TaskRow(FollowUpTask task) {
    id = task.id();
    title = task.title();
    dueOn = task.dueOn();
    completed = task.completed();
  }

  FollowUpTask domain() {
    return new FollowUpTask(id, title, dueOn, completed);
  }
}
