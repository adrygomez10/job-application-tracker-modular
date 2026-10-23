package dev.adriangm.tracker.applications.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** In-process public contract. No persistence entities or domain snapshots cross this boundary. */
public interface ApplicationsApi {
  void validate(Offer offer);

  UUID register(UUID id, Offer offer);

  List<ApplicationView> applications(Filter filter);

  List<InterviewView> interviews(Instant from, Instant until);

  List<ActivityView> openActivities();

  Statistics statistics();

  record Offer(
      String company,
      String role,
      String url,
      String source,
      String location,
      String workMode,
      String technologies) {}

  record Filter(
      String query,
      String status,
      String workMode,
      LocalDate from,
      LocalDate to,
      boolean archived) {}

  record ApplicationView(
      UUID id,
      String company,
      String role,
      String status,
      String workMode,
      LocalDate appliedOn,
      Instant updatedAt) {}

  record InterviewView(
      UUID id,
      UUID applicationId,
      String title,
      String company,
      Instant startsAt,
      String location,
      String url) {}

  record ActivityView(
      UUID id, UUID applicationId, String kind, String title, Instant startsAt, LocalDate dueOn) {}

  record Statistics(Map<String, Long> byStatus, long submitted, long interviewed) {
    public Statistics {
      byStatus = Map.copyOf(byStatus);
    }
  }
}
