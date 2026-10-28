package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Form for the descriptive data. A new form already carries the identity of the future application:
 * submitting it twice is rejected as a conflict instead of creating a duplicate.
 */
public record ApplicationForm(
    @NotNull UUID id,
    @Min(0) long version,
    @NotBlank @Size(max = 160) String company,
    @NotBlank @Size(max = 180) String role,
    @Size(max = 2000) String url,
    @Size(max = 160) String source,
    @Size(max = 160) String location,
    @NotNull WorkMode workMode,
    @Size(max = 500) String technologies,
    @Size(max = 500) String contact,
    @Size(max = 10000) String notes,
    @NotNull Status status,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedOn
) {
    public static ApplicationForm empty()
    {
        return new ApplicationForm(
            UUID.randomUUID(),
            0,
            "",
            "",
            "",
            "",
            "",
            WorkMode.UNSPECIFIED,
            "",
            "",
            "",
            Status.SAVED,
            null
        );
    }

    public static ApplicationForm from(JobApplication application)
    {
        var details = application.details();
        return new ApplicationForm(
            application.id(),
            application.version(),
            details.company(),
            details.role(),
            details.url(),
            details.source(),
            details.location(),
            details.workMode(),
            details.technologies(),
            details.contact(),
            details.notes(),
            application.status(),
            application.appliedOn()
        );
    }

    public ApplicationDetails details()
    {
        return new ApplicationDetails(company, role, url, source, location, workMode, technologies, contact, notes);
    }
}
