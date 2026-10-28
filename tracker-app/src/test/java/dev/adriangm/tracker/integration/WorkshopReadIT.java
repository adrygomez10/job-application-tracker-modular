package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.exports.application.ExportService;
import dev.adriangm.tracker.imports.application.ImportService;
import dev.adriangm.tracker.reminders.application.RefreshReminders;
import dev.adriangm.tracker.reminders.application.ReminderStore;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorkshopReadIT extends IntegrationTest {
    @Autowired
    ApplicationsApi applications;
    @Autowired
    ImportService imports;
    @Autowired
    ExportService exports;
    @Autowired
    RefreshReminders refresh;
    @Autowired
    ReminderStore reminders;

    @Test
    void exportIncludesAllMatchingPagesAndEscapesFormulaCells() throws Exception
    {
        var rows = new StringBuilder("source,external_id,company,role\n");
        for (int index = 0; index < 25; index++) {
            rows.append("demo,").append(index).append(",Example,Java\n");
        }
        rows.append("demo,extra,=FORMULA(),Other\n");
        imports.confirm(rows.toString().getBytes(StandardCharsets.UTF_8));
        var download = exports
            .export(
                "applications-csv",
                new ApplicationsApi.Filter("Example", "", "", null, null, false),
                Instant.EPOCH,
                Instant.now()
            );
        assertThat(new String(download.contents(), StandardCharsets.UTF_8).lines().count()).isEqualTo(26);
        var all = exports
            .export(
                "applications-csv",
                new ApplicationsApi.Filter("", "", "", null, null, false),
                Instant.EPOCH,
                Instant.now()
            );
        assertThat(new String(all.contents(), StandardCharsets.UTF_8)).contains("'=FORMULA()");
    }

    @Test
    void statisticsExcludeSavedAndArchivedFromInterviewDenominator()
    {
        var application = seed();
        assertThat(applications.statistics().submitted()).isZero();
        jdbc.update("UPDATE job_applications SET status = 'APPLIED', applied_on = DATE '2026-10-01'");
        assertThat(applications.statistics().submitted()).isEqualTo(1);
        jdbc.update("UPDATE job_applications SET archived = true");
        assertThat(applications.statistics().submitted()).isZero();
        assertThat(applications.statistics().byStatus()).isEmpty();
        assertThat(application.id()).isNotNull();
    }

    @Test
    void refreshPersistsDeduplicatesAndResolvesChangedActivities()
    {
        var application = seed();
        var taskId = java.util.UUID.randomUUID();
        jdbc
            .update(
                "INSERT INTO application_tasks VALUES (?, 0, ?, 'Follow up', DATE '2020-01-01', false)",
                application.id(),
                taskId
            );
        refresh.execute();
        refresh.execute();
        assertThat(reminders.snapshot().active()).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM reminders", Long.class)).isEqualTo(1);
        jdbc.update("UPDATE application_tasks SET completed = true");
        refresh.execute();
        assertThat(reminders.snapshot().active()).isEmpty();
        assertThat(reminders.snapshot().checkedAt()).isNotNull();
    }

    @Test
    void failedRefreshRollsBackActiveFlagsAndTimestamp()
    {
        var application = seed();
        jdbc
            .update(
                "INSERT INTO application_tasks VALUES (?, 0, ?, 'Follow up', DATE '2020-01-01', false)",
                application.id(),
                java.util.UUID.randomUUID()
            );
        refresh.execute();
        var before = reminders.snapshot();
        jdbc.execute("ALTER TABLE reminders ADD CONSTRAINT test_reminder_failure CHECK (title <> 'Fail')");
        try {
            var invalid = new dev.adriangm.tracker.reminders.application.Reminder(
                java.util.UUID.randomUUID(),
                application.id(),
                "TASK",
                "Fail",
                Instant.EPOCH
            );
            org.assertj.core.api.Assertions
                .assertThatThrownBy(
                    () -> reminders.refresh(java.util.List.of(invalid), before.checkedAt().plusSeconds(60))
                )
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
            assertThat(reminders.snapshot()).isEqualTo(before);
        } finally {
            jdbc.execute("ALTER TABLE reminders DROP CONSTRAINT test_reminder_failure");
        }
    }

    @Test
    void reschedulingAndArchivingResolveOldNoticesAndARecreatedStoreReadsTheSnapshot()
    {
        var application = seed();
        var taskId = java.util.UUID.randomUUID();
        jdbc
            .update(
                "INSERT INTO application_tasks VALUES (?, 0, ?, 'Follow up', DATE '2020-01-01', false)",
                application.id(),
                taskId
            );
        refresh.execute();
        jdbc.update("UPDATE application_tasks SET due_on = DATE '2020-01-02'");
        refresh.execute();
        assertThat(reminders.snapshot().active()).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM reminders", Long.class)).isEqualTo(2);
        var recreated = new dev.adriangm.tracker.reminders.adapter.persistence.JdbcReminderStore(jdbc);
        assertThat(recreated.snapshot()).isEqualTo(reminders.snapshot());
        jdbc.update("UPDATE job_applications SET archived = true");
        refresh.execute();
        assertThat(reminders.snapshot().active()).isEmpty();
    }

    @Test
    void cancelledInterviewDisappearsFromRemindersAndInterviewRate()
    {
        var application = seed();
        jdbc.update("UPDATE job_applications SET status = 'APPLIED', applied_on = DATE '2026-10-01'");
        jdbc
            .update(
                """
                    INSERT INTO application_interviews VALUES
                    (?,0,?,'Interview','INTERVIEW',?,'','','','SCHEDULED','')
                    """,
                application.id(),
                java.util.UUID.randomUUID(),
                java.sql.Timestamp.from(Instant.now().plusSeconds(3600))
            );
        refresh.execute();
        assertThat(reminders.snapshot().active()).hasSize(1);
        assertThat(applications.statistics().interviewed()).isEqualTo(1);
        jdbc.update("UPDATE application_interviews SET outcome = 'CANCELLED'");
        refresh.execute();
        assertThat(reminders.snapshot().active()).isEmpty();
        assertThat(applications.statistics().interviewed()).isZero();
    }
}
