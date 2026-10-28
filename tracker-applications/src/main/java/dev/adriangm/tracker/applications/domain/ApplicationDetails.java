package dev.adriangm.tracker.applications.domain;

import java.util.Objects;

public record ApplicationDetails(
    String company,
    String role,
    String url,
    String source,
    String location,
    WorkMode workMode,
    String technologies,
    String contact,
    String notes
) {
    public ApplicationDetails
    {
        company = Checks.requiredText(company, 160);
        role = Checks.requiredText(role, 180);
        url = Checks.url(url);
        source = Checks.optionalText(source, 160);
        location = Checks.optionalText(location, 160);
        workMode = Objects.requireNonNull(workMode);
        technologies = Checks.optionalText(technologies, 500);
        contact = Checks.optionalText(contact, 500);
        notes = Checks.optionalText(notes, 10000);
    }
}
