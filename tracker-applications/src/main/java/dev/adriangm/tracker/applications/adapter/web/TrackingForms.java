package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.activities.ScheduleInterview;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.StatusChange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.format.annotation.DateTimeFormat;

/** Forms of the application detail page. Each one carries the version the user was editing. */
public final class TrackingForms {
    private TrackingForms()
    {
    }

    public record VersionForm(@Min(0) long version) {
    }

    public record StatusForm(
        @Min(0) long version,
        @NotNull Status status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedOn,
        @Size(max = 10000) String reason,
        Boolean correction
    ) {
        StatusChange change()
        {
            return Boolean.TRUE.equals(correction)
                ? StatusChange.correction(status, appliedOn, reason)
                : StatusChange.update(status, appliedOn, reason);
        }
    }

    public record NoteForm(@Min(0) long version, @NotBlank @Size(max = 10000) String body) {
    }

    public record TaskForm(
        @Min(0) long version,
        @NotBlank @Size(max = 180) String title,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueOn
    ) {
    }

    public record InterviewForm(
        @Min(0) long version,
        @NotBlank @Size(max = 180) String title,
        @NotNull Interview.Kind kind,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
        @Size(max = 500) String location,
        @Size(max = 2000) String url,
        @Size(max = 10000) String notes
    ) {
        /** Interprets the local time in the given zone; a skipped or repeated hour is ambiguous. */
        ScheduleInterview.Request request(ZoneId zone)
        {
            var offsets = zone.getRules().getValidOffsets(startsAt);
            if (offsets.size() != 1) {
                throw new InvalidInput("ambiguousTime");
            }
            return new ScheduleInterview.Request(
                title,
                kind,
                startsAt.toInstant(offsets.getFirst()),
                location,
                url,
                notes
            );
        }
    }

    public record InterviewResultForm(
        @Min(0) long version,
        @NotNull Interview.Outcome outcome,
        @Size(max = 10000) String result
    ) {
    }
}
