package dev.adriangm.tracker.reminders.application;

import java.time.Instant;
import java.util.List;

public interface ReminderStore {
  void refresh(List<Reminder> reminders, Instant checkedAt);

  Snapshot snapshot();

  record Snapshot(List<Reminder> active, Instant checkedAt) {
    public Snapshot {
      active = List.copyOf(active);
    }
  }
}
