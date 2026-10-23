package dev.adriangm.tracker.applications.application;

import static dev.adriangm.tracker.support.Fixtures.CLOCK;
import static dev.adriangm.tracker.support.Fixtures.DIRECT;
import static dev.adriangm.tracker.support.Fixtures.NOW;
import static dev.adriangm.tracker.support.Fixtures.TODAY;
import static dev.adriangm.tracker.support.Fixtures.details;
import static dev.adriangm.tracker.support.Fixtures.saved;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adriangm.tracker.applications.application.activities.AddNote;
import dev.adriangm.tracker.applications.application.activities.ScheduleInterview;
import dev.adriangm.tracker.applications.application.details.RegisterApplication;
import dev.adriangm.tracker.applications.application.status.ChangeStatus;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.StatusChange;
import dev.adriangm.tracker.support.FailingApplicationStore;
import dev.adriangm.tracker.support.MemoryApplicationStore;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UseCasesTest {
  final MemoryApplicationStore store = new MemoryApplicationStore();
  final VersionedUpdate update = new VersionedUpdate(store, DIRECT);
  final ChangeStatus change = new ChangeStatus(update, CLOCK);
  final AddNote addNote = new AddNote(update, CLOCK);

  @Test
  void registerCanRunWithoutSpring() {
    var application =
        new RegisterApplication(store, DIRECT, CLOCK)
            .execute(UUID.randomUUID(), details(), Status.APPLIED, TODAY);
    assertThat(application.createdAt()).isEqualTo(NOW);
    assertThat(store.find(application.id())).contains(application);
  }

  @Test
  void staleFormCannotOverwriteChanges() {
    var application = store.create(saved());
    addNote.execute(application.id(), 0, "First update");
    assertThatThrownBy(
            () ->
                change.execute(application.id(), 0, StatusChange.update(Status.APPLIED, TODAY, "")))
        .isInstanceOf(EditConflict.class);
    assertThat(store.find(application.id()).orElseThrow().status()).isEqualTo(Status.SAVED);
  }

  @Test
  void repeatingStatusDoesNotIncrementVersion() {
    var application = store.create(saved());
    var unchanged =
        change.execute(application.id(), 0, StatusChange.update(Status.SAVED, null, ""));
    assertThat(unchanged.version()).isZero();
  }

  @Test
  void absenceIsExplicit() {
    assertThatThrownBy(() -> addNote.execute(UUID.randomUUID(), 0, "Note"))
        .isInstanceOf(RecordMissing.class);
  }

  @Test
  void newInterviewsAreScheduledByTheUseCase() {
    var application = store.create(saved());
    var request =
        new ScheduleInterview.Request("Interview", Interview.Kind.INTERVIEW, NOW, "", "", "");
    var scheduled = new ScheduleInterview(update, CLOCK).execute(application.id(), 0, request);
    assertThat(scheduled.interviews().getFirst().outcome()).isEqualTo(Interview.Outcome.SCHEDULED);
  }

  @Test
  void failedSaveCannotReturnSuccess() {
    var application = store.create(saved());
    var failing = new VersionedUpdate(new FailingApplicationStore(store), DIRECT);
    assertThatThrownBy(
            () ->
                new ChangeStatus(failing, CLOCK)
                    .execute(application.id(), 0, StatusChange.update(Status.APPLIED, TODAY, "")))
        .hasMessage("Storage unavailable");
    assertThat(store.find(application.id()).orElseThrow().status()).isEqualTo(Status.SAVED);
  }
}
