package dev.adriangm.tracker.applications.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable snapshot of an application. Its operations decide whether a change is valid and return
 * a new snapshot with the matching history entry.
 *
 * <p>The canonical constructor is the reconstruction path used by the persistence adapter. It
 * protects the invariants of a single snapshot (status and submission date); transitions are only
 * checked by the operations. An architecture test keeps every other layer away from it. {@code
 * version} is the optimistic concurrency token that travels with the snapshot.
 */
public record JobApplication(
    UUID id,
    long version,
    ApplicationDetails details,
    Status status,
    LocalDate appliedOn,
    boolean archived,
    Instant createdAt,
    Instant updatedAt,
    List<TimelineEntry> timeline,
    List<Interview> interviews,
    List<FollowUpTask> tasks) {
  public JobApplication {
    Objects.requireNonNull(id);
    Objects.requireNonNull(details);
    Objects.requireNonNull(status);
    Objects.requireNonNull(createdAt);
    Objects.requireNonNull(updatedAt);
    timeline = List.copyOf(timeline);
    interviews = List.copyOf(interviews);
    tasks = List.copyOf(tasks);
    if (status.requiresSubmissionDate() && appliedOn == null) {
      throw new RuleViolation("appliedOn");
    }
    if (status == Status.SAVED && appliedOn != null) {
      throw new RuleViolation("savedDate");
    }
  }

  public static JobApplication create(
      UUID id,
      ApplicationDetails details,
      Status status,
      LocalDate appliedOn,
      Instant now,
      LocalDate today) {
    checkDate(appliedOn, today);
    var created = TimelineEntry.statusChange(now, TimelineEvent.CREATED, null, status, "");
    return new JobApplication(
        id, 0, details, status, appliedOn, false, now, now, List.of(created), List.of(), List.of());
  }

  public JobApplication edit(ApplicationDetails next, Instant now) {
    if (details.equals(next)) {
      return this;
    }
    return withDetails(next).recorded(TimelineEntry.of(now, TimelineEvent.DETAILS_UPDATED, ""));
  }

  public JobApplication changeStatus(StatusChange change, Instant now, LocalDate today) {
    checkDate(change.appliedOn(), today);
    if (change.next() == status && Objects.equals(change.appliedOn(), appliedOn)) {
      return this;
    }
    boolean correcting =
        change.kind() == StatusChange.Kind.CORRECTION
            || requiresCorrection(change.next(), change.appliedOn());
    if (correcting && change.reason().isBlank()) {
      throw new RuleViolation("reason");
    }
    var event = correcting ? TimelineEvent.STATUS_CORRECTED : TimelineEvent.STATUS_CHANGED;
    return withStatus(change.next(), change.appliedOn())
        .recorded(TimelineEntry.statusChange(now, event, status, change.next(), change.reason()));
  }

  private boolean requiresCorrection(Status nextStatus, LocalDate nextDate) {
    boolean revisesExistingDate = appliedOn != null && !Objects.equals(appliedOn, nextDate);
    boolean reversesProgress = nextStatus.progression() < status.progression();
    return status.closed() || reversesProgress || nextStatus == status || revisesExistingDate;
  }

  public JobApplication archive(Instant now) {
    if (archived) {
      return this;
    }
    return withArchived(true).recorded(TimelineEntry.of(now, TimelineEvent.ARCHIVED, ""));
  }

  public JobApplication restore(Instant now) {
    if (!archived) {
      return this;
    }
    return withArchived(false).recorded(TimelineEntry.of(now, TimelineEvent.UNARCHIVED, ""));
  }

  public JobApplication addNote(String text, Instant now) {
    return recorded(TimelineEntry.of(now, TimelineEvent.NOTE, Checks.requiredText(text, 10000)));
  }

  public JobApplication addInterview(Interview interview, Instant now) {
    var next = new ArrayList<>(interviews);
    next.add(interview);
    return withInterviews(next)
        .recorded(TimelineEntry.of(now, TimelineEvent.INTERVIEW_ADDED, interview.title()));
  }

  public JobApplication finishInterview(
      UUID interviewId, Interview.Outcome outcome, String result, Instant now) {
    if (interviews.stream().noneMatch(interview -> interview.id().equals(interviewId))) {
      throw new RuleViolation("interviewMissing");
    }
    var next =
        interviews.stream()
            .map(
                interview ->
                    interview.id().equals(interviewId)
                        ? interview.finish(outcome, result)
                        : interview)
            .toList();
    return withInterviews(next)
        .recorded(TimelineEntry.of(now, TimelineEvent.INTERVIEW_UPDATED, ""));
  }

  public JobApplication addTask(FollowUpTask task, Instant now) {
    var next = new ArrayList<>(tasks);
    next.add(task);
    return withTasks(next).recorded(TimelineEntry.of(now, TimelineEvent.TASK_ADDED, task.title()));
  }

  public JobApplication completeTask(UUID taskId, Instant now) {
    var task =
        tasks.stream()
            .filter(candidateTask -> candidateTask.id().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new RuleViolation("taskMissing"));
    if (task.completed()) {
      return this;
    }
    var next =
        tasks.stream()
            .map(
                candidateTask ->
                    candidateTask.id().equals(taskId) ? candidateTask.complete() : candidateTask)
            .toList();
    return withTasks(next)
        .recorded(TimelineEntry.of(now, TimelineEvent.TASK_COMPLETED, task.title()));
  }

  /** Returns the snapshot with the version assigned by a store after saving it. */
  public JobApplication withVersion(long next) {
    return new JobApplication(
        id,
        next,
        details,
        status,
        appliedOn,
        archived,
        createdAt,
        updatedAt,
        timeline,
        interviews,
        tasks);
  }

  private JobApplication recorded(TimelineEntry entry) {
    var entries = new ArrayList<>(timeline);
    entries.add(entry);
    return new JobApplication(
        id,
        version,
        details,
        status,
        appliedOn,
        archived,
        createdAt,
        entry.occurredAt(),
        entries,
        interviews,
        tasks);
  }

  private JobApplication withDetails(ApplicationDetails next) {
    return new JobApplication(
        id,
        version,
        next,
        status,
        appliedOn,
        archived,
        createdAt,
        updatedAt,
        timeline,
        interviews,
        tasks);
  }

  private JobApplication withStatus(Status nextStatus, LocalDate nextDate) {
    return new JobApplication(
        id,
        version,
        details,
        nextStatus,
        nextDate,
        archived,
        createdAt,
        updatedAt,
        timeline,
        interviews,
        tasks);
  }

  private JobApplication withArchived(boolean next) {
    return new JobApplication(
        id,
        version,
        details,
        status,
        appliedOn,
        next,
        createdAt,
        updatedAt,
        timeline,
        interviews,
        tasks);
  }

  private JobApplication withInterviews(List<Interview> next) {
    return new JobApplication(
        id, version, details, status, appliedOn, archived, createdAt, updatedAt, timeline, next,
        tasks);
  }

  private JobApplication withTasks(List<FollowUpTask> next) {
    return new JobApplication(
        id,
        version,
        details,
        status,
        appliedOn,
        archived,
        createdAt,
        updatedAt,
        timeline,
        interviews,
        next);
  }

  private static void checkDate(LocalDate date, LocalDate today) {
    if (date != null && date.isAfter(today)) {
      throw new RuleViolation("futureApplication");
    }
  }
}
