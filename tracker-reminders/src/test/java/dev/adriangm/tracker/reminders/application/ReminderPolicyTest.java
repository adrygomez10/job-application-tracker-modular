package dev.adriangm.tracker.reminders.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.applications.api.ApplicationsApi.ActivityView;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReminderPolicyTest {
  private static final Instant NOW = Instant.parse("2026-10-24T22:30:00Z");
  private final ReminderPolicy policy =
      new ReminderPolicy(Clock.fixed(NOW, ZoneId.of("Europe/Madrid")));

  @Test
  void tasksUseLocalDateAndInterviewsUseTwentyFourHoursAcrossDst() {
    var yesterday = task(LocalDate.of(2026, 10, 24));
    var today = task(LocalDate.of(2026, 10, 25));
    assertThat(
            policy.due(
                List.of(
                    yesterday,
                    today,
                    interview(NOW),
                    interview(NOW.minusSeconds(1)),
                    interview(NOW.plusSeconds(86400)))))
        .extracting(Reminder::activityId)
        .containsExactly(yesterday.id(), interviewId);
  }

  private final UUID interviewId = UUID.randomUUID();

  private ActivityView interview(Instant startsAt) {
    return new ActivityView(
        interviewId, UUID.randomUUID(), "INTERVIEW", "Interview", startsAt, null);
  }

  private static ActivityView task(LocalDate dueOn) {
    return new ActivityView(UUID.randomUUID(), UUID.randomUUID(), "TASK", "Follow up", null, dueOn);
  }
}
