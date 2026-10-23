package dev.adriangm.tracker.imports.adapter.persistence;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.imports.application.ImportEntry;
import dev.adriangm.tracker.imports.application.ImportLedger;
import dev.adriangm.tracker.imports.application.ImportUnavailable;
import dev.adriangm.tracker.imports.application.OfferWriter;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransactionalOfferWriter implements OfferWriter {
  private final ApplicationsApi applications;
  private final ImportLedger ledger;
  private final JdbcTemplate jdbc;
  private final TransactionTemplate transaction;

  public TransactionalOfferWriter(
      ApplicationsApi applications,
      ImportLedger ledger,
      JdbcTemplate jdbc,
      PlatformTransactionManager manager) {
    this.applications = applications;
    this.ledger = ledger;
    this.jdbc = jdbc;
    this.transaction = new TransactionTemplate(manager);
  }

  @Override
  public String save(ImportEntry entry) {
    try {
      return transaction.execute(status -> saveInsideTransaction(entry));
    } catch (DataAccessException
        | org.springframework.transaction.TransactionException unavailable) {
      throw new ImportUnavailable(unavailable);
    }
  }

  private String saveInsideTransaction(ImportEntry entry) {
    UUID id = UUID.randomUUID();
    int inserted =
        jdbc.update(
            """
        INSERT INTO imported_offers (source, external_id, fingerprint, application_id)
        VALUES (?, ?, ?, ?) ON CONFLICT (source, external_id) DO NOTHING
        """,
            entry.source(),
            entry.externalId(),
            entry.fingerprint(),
            id);
    if (inserted == 0) {
      return ledger
              .fingerprint(entry.source(), entry.externalId())
              .orElseThrow()
              .equals(entry.fingerprint())
          ? "DUPLICATE"
          : "CONFLICT";
    }
    applications.register(id, entry.offer());
    return "IMPORTED";
  }
}
