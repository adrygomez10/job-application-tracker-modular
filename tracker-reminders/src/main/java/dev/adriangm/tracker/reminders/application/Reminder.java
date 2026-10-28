package dev.adriangm.tracker.reminders.application;

import java.time.Instant;
import java.util.UUID;

public record Reminder(UUID activityId, UUID applicationId, String kind, String title, Instant dueAt) {
}
