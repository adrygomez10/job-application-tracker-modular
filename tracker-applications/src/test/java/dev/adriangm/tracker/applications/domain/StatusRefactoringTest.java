package dev.adriangm.tracker.applications.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatusRefactoringTest {
  /** Historical condition, retained only as the workshop's characterization example. */
  boolean legacyActive(Status status) {
    return status != Status.SAVED
        && status != Status.ACCEPTED
        && status != Status.REJECTED
        && status != Status.WITHDRAWN;
  }

  @ParameterizedTest
  @EnumSource(Status.class)
  void extractingThePolicyPreservesKnownBehaviour(Status status) {
    assertThat(status.active()).isEqualTo(legacyActive(status));
    assertThat(status.active())
        .isEqualTo(
            status == Status.APPLIED
                || status == Status.IN_PROGRESS
                || status == Status.OFFER_RECEIVED);
  }
}
