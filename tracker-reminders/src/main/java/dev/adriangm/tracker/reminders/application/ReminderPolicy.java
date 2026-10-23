package dev.adriangm.tracker.reminders.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.ActivityView;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public final class ReminderPolicy {
  private final Clock clock;

  public ReminderPolicy(Clock clock) {
    this.clock = clock;
  }

  public List<Reminder> due(List<ActivityView> activities) {
    return activities.stream().filter(this::isDue).map(this::reminder).toList();
  }

  private boolean isDue(ActivityView activity) {
    if (activity.kind().equals("TASK")) {
      return activity.dueOn().isBefore(LocalDate.now(clock));
    }
    var now = clock.instant();
    return !activity.startsAt().isBefore(now)
        && activity.startsAt().isBefore(now.plus(Duration.ofHours(24)));
  }

  private Reminder reminder(ActivityView activity) {
    var dueAt =
        activity.kind().equals("TASK")
            ? activity.dueOn().atStartOfDay(clock.getZone()).toInstant()
            : activity.startsAt();
    return new Reminder(
        activity.id(), activity.applicationId(), activity.kind(), activity.title(), dueAt);
  }
}
