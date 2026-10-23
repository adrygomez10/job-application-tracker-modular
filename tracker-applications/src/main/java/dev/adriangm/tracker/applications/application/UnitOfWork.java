package dev.adriangm.tracker.applications.application;

import java.util.function.Supplier;

public interface UnitOfWork {
  <T> T required(Supplier<T> work);
}
