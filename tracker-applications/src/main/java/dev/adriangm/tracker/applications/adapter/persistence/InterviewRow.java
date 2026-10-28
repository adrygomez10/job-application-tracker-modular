package dev.adriangm.tracker.applications.adapter.persistence;

import dev.adriangm.tracker.applications.domain.Interview;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.UUID;

@Embeddable
class InterviewRow {
    UUID id;
    String title;

    @Enumerated(EnumType.STRING)
    Interview.Kind kind;

    Instant startsAt;

    @Column(length = 500)
    String location;

    @Column(length = 2000)
    String url;

    @Column(length = 10000)
    String notes;

    @Enumerated(EnumType.STRING)
    Interview.Outcome outcome;

    @Column(length = 10000)
    String result;

    protected InterviewRow()
    {
    }

    InterviewRow(Interview interview)
    {
        id = interview.id();
        title = interview.title();
        kind = interview.kind();
        startsAt = interview.startsAt();
        location = interview.location();
        url = interview.url();
        notes = interview.notes();
        outcome = interview.outcome();
        result = interview.result();
    }

    Interview domain()
    {
        return new Interview(id, title, kind, startsAt, location, url, notes, outcome, result);
    }
}
