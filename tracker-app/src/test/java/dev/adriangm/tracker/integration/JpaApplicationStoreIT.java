package dev.adriangm.tracker.integration;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.UnitOfWork;
import dev.adriangm.tracker.contract.ApplicationStoreContract;

/** Runs the same store contract as the in-memory adapter, now against PostgreSQL. */
class JpaApplicationStoreIT extends IntegrationTest implements ApplicationStoreContract {
  @Override
  public ApplicationStore store() {
    return store;
  }

  @Override
  public UnitOfWork work() {
    return work;
  }
}
