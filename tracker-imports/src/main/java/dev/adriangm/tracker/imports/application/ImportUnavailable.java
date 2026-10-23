package dev.adriangm.tracker.imports.application;

public final class ImportUnavailable extends RuntimeException {
  public ImportUnavailable(Throwable cause) {
    super("Import storage is unavailable", cause);
  }
}
