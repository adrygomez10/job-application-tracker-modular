package dev.adriangm.tracker.reminders.adapter.persistence;

import dev.adriangm.tracker.reminders.application.Reminder;
import dev.adriangm.tracker.reminders.application.ReminderStore;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcReminderStore implements ReminderStore {
  private final JdbcTemplate jdbc;

  public JdbcReminderStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public void refresh(List<Reminder> reminders, Instant checkedAt) {
    jdbc.queryForObject("SELECT id FROM reminder_refresh WHERE id = 1 FOR UPDATE", Integer.class);
    jdbc.update("UPDATE reminders SET active = false");
    for (Reminder reminder : reminders) {
      jdbc.update(
          """
          INSERT INTO reminders (activity_id, application_id, kind, title, due_at, active)
          VALUES (?, ?, ?, ?, ?, true) ON CONFLICT (activity_id, kind, due_at)
          DO UPDATE SET title = EXCLUDED.title, active = true
          """,
          reminder.activityId(),
          reminder.applicationId(),
          reminder.kind(),
          reminder.title(),
          Timestamp.from(reminder.dueAt()));
    }
    jdbc.update(
        "UPDATE reminder_refresh SET checked_at = ? WHERE id = 1", Timestamp.from(checkedAt));
  }

  @Override
  @Transactional(
      readOnly = true,
      isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Snapshot snapshot() {
    var active =
        jdbc.query(
            "SELECT * FROM reminders WHERE active ORDER BY due_at, activity_id",
            (row, index) ->
                new Reminder(
                    row.getObject("activity_id", UUID.class),
                    row.getObject("application_id", UUID.class),
                    row.getString("kind"),
                    row.getString("title"),
                    row.getTimestamp("due_at").toInstant()));
    Instant checkedAt =
        jdbc.queryForObject(
            "SELECT checked_at FROM reminder_refresh WHERE id = 1",
            (row, index) -> row.getTimestamp(1) == null ? null : row.getTimestamp(1).toInstant());
    return new Snapshot(active, checkedAt);
  }
}
