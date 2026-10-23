package dev.adriangm.tracker.contract;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.UnitOfWork;
import dev.adriangm.tracker.support.Fixtures;
import dev.adriangm.tracker.support.MemoryApplicationStore;

class MemoryApplicationStoreTest implements ApplicationStoreContract {
  final ApplicationStore store = new MemoryApplicationStore();

  @Override
  public ApplicationStore store() {
    return store;
  }

  @Override
  public UnitOfWork work() {
    return Fixtures.DIRECT;
  }
}
