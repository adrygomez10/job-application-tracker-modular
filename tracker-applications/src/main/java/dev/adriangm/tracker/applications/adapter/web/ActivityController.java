package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.activities.AddNote;
import dev.adriangm.tracker.applications.application.activities.AddTask;
import dev.adriangm.tracker.applications.application.activities.CompleteTask;
import dev.adriangm.tracker.applications.application.activities.RecordInterviewOutcome;
import dev.adriangm.tracker.applications.application.activities.ScheduleInterview;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Notes, follow-up tasks and interviews registered from the detail page. */
@Controller
public class ActivityController {
    private final AddNote addNote;
    private final AddTask addTask;
    private final CompleteTask completeTask;
    private final ScheduleInterview scheduleInterview;
    private final RecordInterviewOutcome recordOutcome;
    private final Clock clock;

    public ActivityController(
        AddNote addNote,
        AddTask addTask,
        CompleteTask completeTask,
        ScheduleInterview scheduleInterview,
        RecordInterviewOutcome recordOutcome,
        Clock clock
    )
    {
        this.addNote = addNote;
        this.addTask = addTask;
        this.completeTask = completeTask;
        this.scheduleInterview = scheduleInterview;
        this.recordOutcome = recordOutcome;
        this.clock = clock;
    }

    @PostMapping("/applications/{id}/notes")
    String note(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.NoteForm form)
    {
        addNote.execute(id, form.version(), form.body());
        return Redirects.toDetail(id);
    }

    @PostMapping("/applications/{id}/tasks")
    String task(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.TaskForm form)
    {
        addTask.execute(id, form.version(), form.title(), form.dueOn());
        return Redirects.toDetail(id);
    }

    @PostMapping("/applications/{id}/tasks/{taskId}/complete")
    String completeTask(
        @PathVariable UUID id,
        @PathVariable UUID taskId,
        @Valid @ModelAttribute TrackingForms.VersionForm form
    )
    {
        completeTask.execute(id, form.version(), taskId);
        return Redirects.toDetail(id);
    }

    @PostMapping("/applications/{id}/interviews")
    String interview(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.InterviewForm form)
    {
        scheduleInterview.execute(id, form.version(), form.request(clock.getZone()));
        return Redirects.toDetail(id);
    }

    @PostMapping("/applications/{id}/interviews/{interviewId}/result")
    String interviewResult(
        @PathVariable UUID id,
        @PathVariable UUID interviewId,
        @Valid @ModelAttribute TrackingForms.InterviewResultForm form
    )
    {
        recordOutcome.execute(id, form.version(), interviewId, form.outcome(), form.result());
        return Redirects.toDetail(id);
    }
}
