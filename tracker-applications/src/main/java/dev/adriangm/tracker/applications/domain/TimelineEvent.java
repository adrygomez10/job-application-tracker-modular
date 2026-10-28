package dev.adriangm.tracker.applications.domain;

/** Facts recorded in an application's history. Names are stored, so keep them stable. */
public enum TimelineEvent {
    CREATED,
    DETAILS_UPDATED,
    STATUS_CHANGED,
    STATUS_CORRECTED,
    ARCHIVED,
    UNARCHIVED,
    NOTE,
    INTERVIEW_ADDED,
    INTERVIEW_UPDATED,
    TASK_ADDED,
    TASK_COMPLETED
}
