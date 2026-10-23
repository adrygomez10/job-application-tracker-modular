package dev.adriangm.tracker.imports.fixture;

import dev.adriangm.tracker.applications.domain.Status;

/** Deliberately invalid fixture, excluded from normal production imports. */
public final class IllegalModuleDependency {
  public Status state() {
    return Status.SAVED;
  }
}
