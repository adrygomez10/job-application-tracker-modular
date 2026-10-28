package dev.adriangm.tracker.imports.adapter.persistence;

import dev.adriangm.tracker.imports.application.ImportLedger;
import dev.adriangm.tracker.imports.application.ImportUnavailable;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcImportLedger implements ImportLedger {
    private final JdbcTemplate jdbc;

    public JdbcImportLedger(JdbcTemplate jdbc)
    {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> fingerprint(String source, String externalId)
    {
        try {
            return jdbc
                .query(
                    "SELECT fingerprint FROM imported_offers WHERE source = ? AND external_id = ?",
                    (row, index) -> row.getString(1),
                    source,
                    externalId
                )
                .stream()
                .findFirst();
        } catch (DataAccessException unavailable) {
            throw new ImportUnavailable(unavailable);
        }
    }
}
