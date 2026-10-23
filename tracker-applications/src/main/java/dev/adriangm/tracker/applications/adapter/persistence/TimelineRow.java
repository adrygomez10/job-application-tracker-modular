package dev.adriangm.tracker.applications.adapter.persistence;

import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.TimelineEntry;
import dev.adriangm.tracker.applications.domain.TimelineEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.UUID;

@Embeddable
class TimelineRow {
  UUID id;
  Instant occurredAt;

  @Enumerated(EnumType.STRING)
  TimelineEvent kind;

  @Enumerated(EnumType.STRING)
  Status previousStatus;

  @Enumerated(EnumType.STRING)
  Status nextStatus;

  @Column(length = 10000)
  String body;

  protected TimelineRow() {}

  TimelineRow(TimelineEntry entry) {
    id = entry.id();
    occurredAt = entry.occurredAt();
    kind = entry.kind();
    previousStatus = entry.previousStatus();
    nextStatus = entry.nextStatus();
    body = entry.body();
  }

  TimelineEntry domain() {
    return new TimelineEntry(id, occurredAt, kind, previousStatus, nextStatus, body);
  }
}
