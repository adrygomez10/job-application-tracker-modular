package dev.adriangm.tracker.support;

import dev.adriangm.tracker.applications.application.UnitOfWork;
import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Supplier;

public final class Fixtures {
    public static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 22);
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneId.of("Europe/Madrid"));
    public static final UnitOfWork DIRECT = new UnitOfWork() {
        @Override
        public <T> T required(Supplier<T> work)
        {
            return work.get();
        }
    };

    public static ApplicationDetails details()
    {
        return new ApplicationDetails(
            "Example Labs",
            "Java developer",
            "https://example.com/jobs/1",
            "Example",
            "Madrid",
            WorkMode.HYBRID,
            "Java, Spring",
            "Recruiting team",
            ""
        );
    }

    public static JobApplication saved()
    {
        return JobApplication.create(UUID.randomUUID(), details(), Status.SAVED, null, NOW, TODAY);
    }

    private Fixtures()
    {
    }
}
