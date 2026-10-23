package dev.adriangm.tracker.exports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.exports.api.ExportData;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public final class ApplicationsDataset implements DatasetSource {
  private final ApplicationsApi applications;
  private final Clock clock;

  public ApplicationsDataset(ApplicationsApi applications, Clock clock) {
    this.applications = applications;
    this.clock = clock;
  }

  @Override
  public String dataset() {
    return "applications";
  }

  @Override
  public ExportData load(ApplicationsApi.Filter filter, Instant from, Instant until) {
    var rows =
        applications.applications(filter).stream()
            .map(
                row ->
                    List.of(
                        row.id().toString(),
                        row.company(),
                        row.role(),
                        row.status(),
                        row.workMode(),
                        row.appliedOn() == null ? "" : row.appliedOn().toString(),
                        row.updatedAt().toString()))
            .toList();
    return new ExportData(
        dataset(),
        List.of("id", "company", "role", "status", "work_mode", "applied_on", "updated_at"),
        rows,
        clock.instant());
  }
}
