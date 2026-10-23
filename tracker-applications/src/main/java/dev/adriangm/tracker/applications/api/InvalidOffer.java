package dev.adriangm.tracker.applications.api;

public final class InvalidOffer extends IllegalArgumentException {
  public InvalidOffer(String message, Throwable cause) {
    super(message, cause);
  }
}
