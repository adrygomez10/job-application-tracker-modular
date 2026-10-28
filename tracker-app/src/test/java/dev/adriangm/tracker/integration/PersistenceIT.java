package dev.adriangm.tracker.integration;

import static dev.adriangm.tracker.support.Fixtures.NOW;
import static dev.adriangm.tracker.support.Fixtures.TODAY;
import static dev.adriangm.tracker.support.Fixtures.details;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adriangm.tracker.applications.application.EditConflict;
import dev.adriangm.tracker.applications.application.activities.AddTask;
import dev.adriangm.tracker.applications.application.activities.CompleteTask;
import dev.adriangm.tracker.applications.application.activities.ScheduleInterview;
import dev.adriangm.tracker.applications.application.details.ArchiveApplication;
import dev.adriangm.tracker.applications.application.query.ApplicationQueries;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.StatusChange;
import dev.adriangm.tracker.applications.domain.TimelineEvent;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

class PersistenceIT extends IntegrationTest {
    private static final int LIMIT = 10;

    @Autowired
    AddTask addTask;
    @Autowired
    CompleteTask completeTask;
    @Autowired
    ScheduleInterview scheduleInterview;
    @Autowired
    ArchiveApplication archive;

    @Test
    void migrationsAreAppliedAndHibernateValidatesThem()
    {
        assertThat(
            jdbc.queryForObject("select count(*) from flyway_schema_history where success = true", Integer.class)
        ).isEqualTo(3);
    }

    @Test
    void timelineKindsAreStoredByName()
    {
        var application = seed();
        assertThat(
            jdbc
                .queryForObject(
                    "select kind from application_timeline where application_id = ?",
                    String.class,
                    application.id()
                )
        ).isEqualTo(TimelineEvent.CREATED.name());
    }

    @Test
    void failureRollsBackStatusAndTimelineTogether()
    {
        var application = seed();
        assertThatThrownBy(() -> work.required(() -> {
            var current = store.find(application.id()).orElseThrow();
            var change = StatusChange.update(Status.APPLIED, TODAY, "");
            store.save(current.changeStatus(change, NOW, TODAY), 0);
            throw new IllegalStateException("Simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        var unchanged = read(application.id());
        assertThat(unchanged.status()).isEqualTo(Status.SAVED);
        assertThat(unchanged.timeline()).hasSize(1);
    }

    @Test
    void concurrentUpdatesHaveExactlyOneWinner() throws Exception
    {
        var application = seed();
        var ready = new CountDownLatch(2);
        var proceed = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> edit = () -> {
                try {
                    work.required(() -> {
                        var current = store.find(application.id()).orElseThrow();
                        ready.countDown();
                        awaitQuietly(proceed);
                        store.save(current.addNote("Concurrent update", NOW), 0);
                        return null;
                    });
                    return true;
                } catch (EditConflict | OptimisticLockingFailureException lost) {
                    return false;
                }
            };
            var first = executor.submit(edit);
            var second = executor.submit(edit);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            proceed.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(true, false);
        }
        assertThat(read(application.id()).timeline()).hasSize(2);
    }

    @Test
    void childChangesAlsoIncrementParentVersion()
    {
        var application = seed();
        var next = addTask.execute(application.id(), application.version(), "Prepare", TODAY);
        assertThat(next.version()).isEqualTo(1);
        assertThat(read(application.id()).tasks()).hasSize(1);
    }

    @Test
    void searchEscapesWildcardsAndFiltersArchivedRecords()
    {
        var application = seed();
        assertThat(search("%", false)).isZero();
        assertThat(
            queries
                .search(new ApplicationQueries.Criteria("example", Status.SAVED, WorkMode.HYBRID, null, null, false, 0))
                .total()
        ).isEqualTo(1);
        archive.execute(application.id(), 0);
        assertThat(search("", false)).isZero();
        assertThat(search("", true)).isEqualTo(1);
    }

    @Test
    void listIsPaginatedAndDateFiltersAreInclusive()
    {
        for (int index = 0; index < ApplicationQueries.PAGE_SIZE + 1; index++) {
            work
                .required(
                    () -> store
                        .create(JobApplication.create(UUID.randomUUID(), details(), Status.APPLIED, TODAY, NOW, TODAY))
                );
        }
        var first = queries.search(new ApplicationQueries.Criteria("", Status.APPLIED, null, TODAY, TODAY, false, 0));
        var second = queries.search(new ApplicationQueries.Criteria("", Status.APPLIED, null, TODAY, TODAY, false, 1));
        assertThat(first.items()).hasSize(ApplicationQueries.PAGE_SIZE);
        assertThat(second.items()).hasSize(1);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.items())
            .extracting(ApplicationQueries.Summary::id)
            .doesNotContain(second.items().getFirst().id());
    }

    @Test
    void agendaAndCountsExcludeArchivedAndCompletedItems()
    {
        var now = Instant.now();
        var application = seed();
        application = addTask.execute(application.id(), application.version(), "Follow up", TODAY.minusDays(1));
        application = scheduleInterview
            .execute(application.id(), application.version(), interviewAt(now.plusSeconds(86400)));
        assertThat(queries.pendingTasks(LIMIT)).hasSize(1);
        assertThat(queries.upcomingInterviews(now, LIMIT)).hasSize(1);
        assertThat(queries.counts().of(Status.SAVED)).isEqualTo(1);
        application = completeTask
            .execute(application.id(), application.version(), application.tasks().getFirst().id());
        assertThat(queries.pendingTasks(LIMIT)).isEmpty();
        archive.execute(application.id(), application.version());
        assertThat(queries.upcomingInterviews(now, LIMIT)).isEmpty();
        assertThat(queries.counts().of(Status.SAVED)).isZero();
    }

    @Test
    void pastScheduledInterviewsAwaitTheirResult()
    {
        var now = Instant.now();
        var application = seed();
        scheduleInterview.execute(application.id(), 0, interviewAt(now.minusSeconds(3600)));
        assertThat(queries.upcomingInterviews(now, LIMIT)).isEmpty();
        assertThat(queries.interviewsAwaitingResult(now, LIMIT))
            .extracting(ApplicationQueries.UpcomingInterview::applicationId)
            .containsExactly(application.id());
    }

    private long search(String query, boolean archived)
    {
        return queries.search(new ApplicationQueries.Criteria(query, null, null, null, null, archived, 0)).total();
    }

    private static ScheduleInterview.Request interviewAt(Instant startsAt)
    {
        return new ScheduleInterview.Request("Interview", Interview.Kind.INTERVIEW, startsAt, "", "", "");
    }

    private static void awaitQuietly(CountDownLatch latch)
    {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Race timed out");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
