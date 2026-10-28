package dev.adriangm.tracker.exports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.exports.api.ExportData;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public final class InterviewsDataset implements DatasetSource {
    private final ApplicationsApi applications;
    private final Clock clock;

    public InterviewsDataset(ApplicationsApi applications, Clock clock)
    {
        this.applications = applications;
        this.clock = clock;
    }

    @Override
    public String dataset()
    {
        return "interviews";
    }

    @Override
    public ExportData load(ApplicationsApi.Filter filter, Instant from, Instant until)
    {
        var rows = applications
            .interviews(from, until)
            .stream()
            .map(
                row -> List
                    .of(
                        row.id().toString(),
                        row.title(),
                        row.company(),
                        row.startsAt().toString(),
                        row.location(),
                        row.url()
                    )
            )
            .toList();
        return new ExportData(
            dataset(),
            List.of("id", "title", "company", "starts_at", "location", "url"),
            rows,
            clock.instant()
        );
    }
}
