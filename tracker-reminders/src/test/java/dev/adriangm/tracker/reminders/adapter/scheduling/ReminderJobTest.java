package dev.adriangm.tracker.reminders.adapter.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.reminders.application.RefreshReminders;
import dev.adriangm.tracker.reminders.application.ReminderPolicy;
import dev.adriangm.tracker.reminders.application.ReminderStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

class ReminderJobTest {
    @Test
    void failedJobPreservesStorageAndNextSuccessfulRunClearsFailure()
    {
        var applications = mock(ApplicationsApi.class);
        var store = mock(ReminderStore.class);
        var clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        when(applications.openActivities())
            .thenThrow(new DataAccessResourceFailureException("Test failure"))
            .thenReturn(List.of());
        var job = new ReminderJob(new RefreshReminders(applications, new ReminderPolicy(clock), store, clock));
        job.run();
        assertThat(job.failed()).isTrue();
        verifyNoInteractions(store);
        job.run();
        assertThat(job.failed()).isFalse();
    }
}
