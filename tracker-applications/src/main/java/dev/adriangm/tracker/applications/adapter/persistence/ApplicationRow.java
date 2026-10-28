package dev.adriangm.tracker.applications.adapter.persistence;

import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
class ApplicationRow {
    @Id
    UUID id;
    @Version
    Long version;
    long mutationCount;
    String company;
    String role;

    @Column(length = 2000)
    String url;

    String source;
    String location;

    @Enumerated(EnumType.STRING)
    WorkMode workMode;

    @Column(length = 500)
    String technologies;

    @Column(length = 500)
    String contact;

    @Column(length = 10000)
    String notes;

    @Enumerated(EnumType.STRING)
    Status status;

    LocalDate appliedOn;
    boolean archived;
    Instant createdAt;
    Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "application_timeline", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "position")
    List<TimelineRow> timeline = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "application_interviews", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "position")
    List<InterviewRow> interviews = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "application_tasks", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "position")
    List<TaskRow> tasks = new ArrayList<>();

    protected ApplicationRow()
    {
    }

    static ApplicationRow from(JobApplication application)
    {
        var row = new ApplicationRow();
        row.id = application.id();
        row.assign(application);
        return row;
    }

    void assign(JobApplication application)
    {
        var details = application.details();
        company = details.company();
        role = details.role();
        url = details.url();
        source = details.source();
        location = details.location();
        workMode = details.workMode();
        technologies = details.technologies();
        contact = details.contact();
        notes = details.notes();
        status = application.status();
        appliedOn = application.appliedOn();
        archived = application.archived();
        createdAt = application.createdAt();
        updatedAt = application.updatedAt();
        mutationCount++;
        timeline.clear();
        timeline.addAll(application.timeline().stream().map(TimelineRow::new).toList());
        interviews.clear();
        interviews.addAll(application.interviews().stream().map(InterviewRow::new).toList());
        tasks.clear();
        tasks.addAll(application.tasks().stream().map(TaskRow::new).toList());
    }

    JobApplication domain()
    {
        return new JobApplication(
            id,
            version,
            new ApplicationDetails(company, role, url, source, location, workMode, technologies, contact, notes),
            status,
            appliedOn,
            archived,
            createdAt,
            updatedAt,
            timeline.stream().map(TimelineRow::domain).toList(),
            interviews.stream().map(InterviewRow::domain).toList(),
            tasks.stream().map(TaskRow::domain).toList()
        );
    }
}
