package dev.adriangm.tracker.applications.domain;

public enum Status {
  SAVED,
  APPLIED,
  IN_PROGRESS,
  OFFER_RECEIVED,
  ACCEPTED,
  REJECTED,
  WITHDRAWN;

  public boolean closed() {
    return this == ACCEPTED || this == REJECTED || this == WITHDRAWN;
  }

  public boolean active() {
    return this != SAVED && !closed();
  }

  /** An offer withdrawn before sending it is valid without a submission date. */
  boolean requiresSubmissionDate() {
    return this != SAVED && this != WITHDRAWN;
  }

  public int progression() {
    return switch (this) {
      case SAVED -> 0;
      case APPLIED -> 1;
      case IN_PROGRESS -> 2;
      case OFFER_RECEIVED -> 3;
      default -> 4;
    };
  }
}
