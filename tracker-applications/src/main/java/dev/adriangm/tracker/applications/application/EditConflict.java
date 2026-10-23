package dev.adriangm.tracker.applications.application;

public final class EditConflict extends RuntimeException {
  public EditConflict() {
    this(null);
  }

  public EditConflict(Throwable cause) {
    super("Application version has changed or already exists. Reload before saving.", cause);
  }
}
