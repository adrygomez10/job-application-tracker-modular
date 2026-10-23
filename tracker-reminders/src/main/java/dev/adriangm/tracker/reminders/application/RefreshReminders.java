package dev.adriangm.tracker.reminders.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import java.time.Clock;

public final class RefreshReminders {
  private final ApplicationsApi applications;
  private final ReminderPolicy policy;
  private final ReminderStore store;
  private final Clock clock;

  public RefreshReminders(
      ApplicationsApi applications, ReminderPolicy policy, ReminderStore store, Clock clock) {
    this.applications = applications;
    this.policy = policy;
    this.store = store;
    this.clock = clock;
  }

  public void execute() {
    store.refresh(policy.due(applications.openActivities()), clock.instant());
  }
}
