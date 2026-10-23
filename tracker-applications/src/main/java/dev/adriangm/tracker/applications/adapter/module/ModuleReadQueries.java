package dev.adriangm.tracker.applications.adapter.module;

import dev.adriangm.tracker.applications.api.ApplicationsApi.ActivityView;
import dev.adriangm.tracker.applications.api.ApplicationsApi.InterviewView;
import dev.adriangm.tracker.applications.api.ApplicationsApi.Statistics;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Queries owned by applications. Consumers never know these tables. */
@Repository
@Transactional(readOnly = true)
public class ModuleReadQueries {
  private final JdbcTemplate jdbc;

  public ModuleReadQueries(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<InterviewView> interviews(Instant from, Instant until) {
    return jdbc.query(
        """
        SELECT i.id, a.id AS application_id, i.title, a.company, i.starts_at, i.location, i.url
        FROM application_interviews i JOIN job_applications a ON a.id = i.application_id
        WHERE NOT a.archived AND i.outcome = 'SCHEDULED'
          AND i.starts_at >= ? AND i.starts_at < ? ORDER BY i.starts_at, i.id
        """,
        (row, index) ->
            new InterviewView(
                row.getObject("id", UUID.class),
                row.getObject("application_id", UUID.class),
                row.getString("title"),
                row.getString("company"),
                row.getTimestamp("starts_at").toInstant(),
                row.getString("location"),
                row.getString("url")),
        Timestamp.from(from),
        Timestamp.from(until));
  }

  public List<ActivityView> openActivities() {
    List<ActivityView> result =
        new ArrayList<>(
            jdbc.query(
                """
        SELECT t.id, a.id AS application_id, t.title, t.due_on
        FROM application_tasks t JOIN job_applications a ON a.id = t.application_id
        WHERE NOT a.archived AND NOT t.completed ORDER BY t.due_on, t.id
        """,
                (row, index) ->
                    new ActivityView(
                        row.getObject("id", UUID.class),
                        row.getObject("application_id", UUID.class),
                        "TASK",
                        row.getString("title"),
                        null,
                        row.getObject("due_on", LocalDate.class))));
    result.addAll(
        jdbc.query(
            """
        SELECT i.id, a.id AS application_id, i.title, i.starts_at
        FROM application_interviews i JOIN job_applications a ON a.id = i.application_id
        WHERE NOT a.archived AND i.outcome = 'SCHEDULED' ORDER BY i.starts_at, i.id
        """,
            (row, index) ->
                new ActivityView(
                    row.getObject("id", UUID.class),
                    row.getObject("application_id", UUID.class),
                    "INTERVIEW",
                    row.getString("title"),
                    row.getTimestamp("starts_at").toInstant(),
                    null)));
    return List.copyOf(result);
  }

  public Statistics statistics() {
    var counts = new LinkedHashMap<String, Long>();
    jdbc.query(
        "SELECT status, count(*) AS total FROM job_applications"
            + " WHERE NOT archived GROUP BY status ORDER BY status",
        row -> {
          counts.put(row.getString("status"), row.getLong("total"));
        });
    long submitted =
        jdbc.queryForObject(
            "SELECT count(*) FROM job_applications"
                + " WHERE NOT archived AND applied_on IS NOT NULL",
            Long.class);
    long interviewed =
        jdbc.queryForObject(
            """
        SELECT count(*) FROM job_applications a WHERE NOT a.archived
        AND a.applied_on IS NOT NULL AND EXISTS
        (SELECT 1 FROM application_interviews i WHERE i.application_id = a.id
        AND i.outcome <> 'CANCELLED')
        """,
            Long.class);
    return new Statistics(counts, submitted, interviewed);
  }
}
