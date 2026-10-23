package dev.adriangm.tracker.imports.application;

public final class InvalidCsv extends IllegalArgumentException {
  public InvalidCsv(String message) {
    super(message);
  }

  public InvalidCsv(String message, Throwable cause) {
    super(message, cause);
  }
}
