package dev.adriangm.tracker.applications.domain;

import static dev.adriangm.tracker.support.Fixtures.NOW;
import static dev.adriangm.tracker.support.Fixtures.TODAY;
import static dev.adriangm.tracker.support.Fixtures.details;
import static dev.adriangm.tracker.support.Fixtures.saved;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class JobApplicationTest {
  @Nested
  class StatusTransitions {
    @Test
    void savedOfferHasNoSubmissionDate() {
      assertThat(saved().appliedOn()).isNull();
    }

    @ParameterizedTest
    @EnumSource(
        value = Status.class,
        names = {"SAVED", "WITHDRAWN"},
        mode = EnumSource.Mode.EXCLUDE)
    void submittedStatusesRequireDate(Status status) {
      assertThatThrownBy(
              () -> JobApplication.create(UUID.randomUUID(), details(), status, null, NOW, TODAY))
          .isInstanceOf(RuleViolation.class)
          .hasMessageContaining("appliedOn");
    }

    @Test
    void savedStatusRejectsSubmissionDate() {
      assertThatThrownBy(
              () ->
                  JobApplication.create(
                      UUID.randomUUID(), details(), Status.SAVED, TODAY, NOW, TODAY))
          .hasMessageContaining("savedDate");
    }

    @Test
    void futureSubmissionIsRejected() {
      assertThatThrownBy(
              () ->
                  JobApplication.create(
                      UUID.randomUUID(), details(), Status.APPLIED, TODAY.plusDays(1), NOW, TODAY))
          .hasMessageContaining("futureApplication");
    }

    @Test
    void stagesMayBeSkipped() {
      var changed =
          saved()
              .changeStatus(
                  StatusChange.update(Status.OFFER_RECEIVED, TODAY, "Direct offer"), NOW, TODAY);
      assertThat(changed.status()).isEqualTo(Status.OFFER_RECEIVED);
      assertThat(changed.timeline()).hasSize(2);
    }

    @Test
    void repeatingStatusHasNoEffect() {
      var application = saved();
      assertThat(application.changeStatus(StatusChange.update(Status.SAVED, null, ""), NOW, TODAY))
          .isSameAs(application);
    }

    @ParameterizedTest
    @EnumSource(
        value = Status.class,
        names = {"ACCEPTED", "REJECTED", "WITHDRAWN"})
    void reopeningClosedProcessRequiresReason(Status status) {
      var closed = JobApplication.create(UUID.randomUUID(), details(), status, TODAY, NOW, TODAY);
      assertThatThrownBy(
              () ->
                  closed.changeStatus(
                      StatusChange.update(Status.IN_PROGRESS, TODAY, " "), NOW, TODAY))
          .hasMessageContaining("reason");
      assertThat(
              closed
                  .changeStatus(
                      StatusChange.update(Status.IN_PROGRESS, TODAY, "New interview"), NOW, TODAY)
                  .status())
          .isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void creationIsTheFirstHistoryEntry() {
      var created = saved().timeline().getFirst();
      assertThat(created.kind()).isEqualTo(TimelineEvent.CREATED);
      assertThat(created.nextStatus()).isEqualTo(Status.SAVED);
    }

    @Test
    void correctionRequiresReason() {
      assertThatThrownBy(
              () ->
                  saved()
                      .changeStatus(StatusChange.correction(Status.APPLIED, TODAY, ""), NOW, TODAY))
          .hasMessageContaining("reason");
    }

    @Test
    void returningToAnEarlierStageIsTreatedAsCorrection() {
      var application =
          JobApplication.create(
              UUID.randomUUID(), details(), Status.IN_PROGRESS, TODAY, NOW, TODAY);
      assertThatThrownBy(
              () ->
                  application.changeStatus(
                      StatusChange.update(Status.APPLIED, TODAY, ""), NOW, TODAY))
          .hasMessageContaining("reason");
    }

    @Test
    void changingOnlySubmissionDateRequiresReason() {
      var application =
          JobApplication.create(UUID.randomUUID(), details(), Status.APPLIED, TODAY, NOW, TODAY);
      assertThatThrownBy(
              () ->
                  application.changeStatus(
                      StatusChange.update(Status.APPLIED, TODAY.minusDays(1), ""), NOW, TODAY))
          .hasMessageContaining("reason");
    }

    @Test
    void correctionPreservesPreviousStatus() {
      var application =
          saved()
              .changeStatus(
                  StatusChange.correction(Status.IN_PROGRESS, TODAY, "Entered late"), NOW, TODAY);
      assertThat(application.timeline().getLast().kind()).isEqualTo(TimelineEvent.STATUS_CORRECTED);
      assertThat(application.timeline().getLast().previousStatus()).isEqualTo(Status.SAVED);
    }
  }

  @Nested
  class ActivityTracking {
    @Test
    void archivingDoesNotChangeStatus() {
      var application = saved().archive(NOW);
      assertThat(application.archived()).isTrue();
      assertThat(application.status()).isEqualTo(Status.SAVED);
      assertThat(application.archive(NOW)).isSameAs(application);
    }

    @Test
    void restoringIsRecordedAndIdempotent() {
      var application = saved().archive(NOW).restore(NOW);
      assertThat(application.archived()).isFalse();
      assertThat(application.timeline().getLast().kind()).isEqualTo(TimelineEvent.UNARCHIVED);
      assertThat(application.restore(NOW)).isSameAs(application);
    }

    @Test
    void editingWithSameDetailsHasNoEffect() {
      var application = saved();
      assertThat(application.edit(details(), NOW)).isSameAs(application);
    }

    @Test
    void taskCompletionIsIdempotent() {
      var task = FollowUpTask.pending(UUID.randomUUID(), "Follow up", TODAY.minusDays(1));
      var application = saved().addTask(task, NOW).completeTask(task.id(), NOW);
      assertThat(application.tasks().getFirst().completed()).isTrue();
      assertThat(application.completeTask(task.id(), NOW)).isSameAs(application);
    }

    @Test
    void unknownTaskIsRejected() {
      assertThatThrownBy(() -> saved().completeTask(UUID.randomUUID(), NOW))
          .hasMessageContaining("taskMissing");
    }

    @Test
    void interviewKeepsItsOutcomeAndResult() {
      var interview =
          Interview.schedule(
              UUID.randomUUID(),
              "Technical interview",
              Interview.Kind.INTERVIEW,
              NOW,
              "Madrid",
              "",
              "Prepare");
      var application =
          saved()
              .addInterview(interview, NOW)
              .finishInterview(interview.id(), Interview.Outcome.COMPLETED, "Second round", NOW);
      assertThat(application.interviews().getFirst().result()).isEqualTo("Second round");
      assertThat(application.interviews().getFirst().outcome())
          .isEqualTo(Interview.Outcome.COMPLETED);
    }

    @Test
    void interviewCannotBeFinishedAsScheduled() {
      var interview =
          Interview.schedule(UUID.randomUUID(), "Call", Interview.Kind.OTHER, NOW, "", "", "");
      var application = saved().addInterview(interview, NOW);
      assertThatThrownBy(
              () ->
                  application.finishInterview(interview.id(), Interview.Outcome.SCHEDULED, "", NOW))
          .hasMessageContaining("interviewOutcome");
    }

    @Test
    void unknownInterviewIsRejected() {
      assertThatThrownBy(
              () ->
                  saved().finishInterview(UUID.randomUUID(), Interview.Outcome.CANCELLED, "", NOW))
          .hasMessageContaining("interviewMissing");
    }

    @Test
    void emptyNotesAreRejected() {
      assertThatThrownBy(() -> saved().addNote(" ", NOW)).hasMessageContaining("required");
    }

    @Test
    void historyCannotBeMutatedOutsideModel() {
      assertThatThrownBy(() -> saved().timeline().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  class DetailsValidation {
    @ParameterizedTest
    @ValueSource(
        strings = {
          "javascript:alert(1)",
          "ftp://example.com/x",
          "https://user:password@example.com",
          "garbage"
        })
    void unsafeUrlsAreRejected(String url) {
      assertThatThrownBy(
              () ->
                  new ApplicationDetails(
                      "Example", "Developer", url, "", "", WorkMode.REMOTE, "", "", ""))
          .hasMessageContaining("url");
    }

    @Test
    void missingCompanyIsRejected() {
      assertThatThrownBy(
              () ->
                  new ApplicationDetails(" ", "Developer", "", "", "", WorkMode.REMOTE, "", "", ""))
          .hasMessageContaining("required");
    }

    @Test
    void longRoleIsRejected() {
      assertThatThrownBy(
              () ->
                  new ApplicationDetails(
                      "Example", "x".repeat(181), "", "", "", WorkMode.REMOTE, "", "", ""))
          .hasMessageContaining("tooLong");
    }

    @Test
    void textIsTrimmedAndOptionalNullsBecomeEmpty() {
      var details =
          new ApplicationDetails(
              " Example ", " Developer ", null, null, null, WorkMode.REMOTE, null, null, null);
      assertThat(details.company()).isEqualTo("Example");
      assertThat(details.notes()).isEmpty();
    }
  }
}
