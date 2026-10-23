package dev.adriangm.tracker.applications.application.query;

import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Read contract the application offers to its entry points and the persistence adapter implements.
 * Reads apply no business rules, so they do not go through a use case. Implementations open their
 * own read-only transaction and never return null.
 */
public interface ApplicationQueries {
  int PAGE_SIZE = 20;

  Results search(Criteria criteria);

  Optional<JobApplication> detail(UUID id);

  StatusCounts counts();

  List<UpcomingInterview> upcomingInterviews(Instant from, int limit);

  List<UpcomingInterview> interviewsAwaitingResult(Instant before, int limit);

  List<PendingTask> pendingTasks(int limit);

  record Criteria(
      String query,
      Status status,
      WorkMode workMode,
      LocalDate from,
      LocalDate to,
      boolean archived,
      int page) {
    public Criteria {
      query = query == null ? "" : query.strip();
      if (query.length() > 180 || page < 0 || isReversedRange(from, to)) {
        throw new InvalidCriteria();
      }
    }

    private static boolean isReversedRange(LocalDate from, LocalDate to) {
      return from != null && to != null && from.isAfter(to);
    }
  }

  record Summary(
      UUID id,
      String company,
      String role,
      Status status,
      WorkMode workMode,
      LocalDate appliedOn,
      Instant updatedAt) {}

  record Results(List<Summary> items, long total, int page, int totalPages) {}

  /** Applications per status, excluding archived ones, in declaration order. */
  record StatusCounts(Map<Status, Long> byStatus) {
    public StatusCounts {
      var complete = new EnumMap<Status, Long>(Status.class);
      for (Status status : Status.values()) {
        complete.put(status, byStatus.getOrDefault(status, 0L));
      }
      byStatus = Collections.unmodifiableMap(complete);
    }

    public long of(Status status) {
      return byStatus.get(status);
    }

    public long active() {
      return byStatus.entrySet().stream()
          .filter(entry -> entry.getKey().active())
          .mapToLong(Map.Entry::getValue)
          .sum();
    }
  }

  record UpcomingInterview(
      UUID applicationId, String company, String role, String title, Instant startsAt) {}

  record PendingTask(
      UUID applicationId, String company, String role, String title, LocalDate dueOn) {}
}
