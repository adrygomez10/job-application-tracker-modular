package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MigrationIT extends IntegrationTest {
  private static final String SCHEMA = "workshop_migration_check";
  @Autowired DataSource dataSource;

  @Test
  void upgradesWorkshopOneWithoutLosingIdentityNotesOrActivities() throws Exception {
    Flyway.configure()
        .dataSource(dataSource)
        .schemas(SCHEMA)
        .defaultSchema(SCHEMA)
        .locations("classpath:db/migration")
        .target("1")
        .load()
        .migrate();
    UUID id = UUID.randomUUID();
    try (Connection connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      seedBaseline(statement, id);
      Flyway.configure()
          .dataSource(dataSource)
          .schemas(SCHEMA)
          .defaultSchema(SCHEMA)
          .locations("classpath:db/migration")
          .load()
          .migrate();
      verifyPreserved(statement, id);
    } finally {
      jdbc.execute("DROP SCHEMA workshop_migration_check CASCADE");
    }
  }

  private static void seedBaseline(Statement statement, UUID id) throws SQLException {
    statement.execute(
        """
        INSERT INTO workshop_migration_check.job_applications VALUES
        ('%s',7,3,'Example','Java','','','','UNSPECIFIED','','','Preserved note',
        'APPLIED','2026-10-01',false,'2026-10-01T10:00:00Z','2026-10-02T10:00:00Z')
        """
            .formatted(id));
    statement.execute(
        "INSERT INTO workshop_migration_check.application_tasks VALUES ('"
            + id
            + "',0,'"
            + UUID.randomUUID()
            + "','Follow up','2026-10-20',false)");
    statement.execute(
        "INSERT INTO workshop_migration_check.application_timeline VALUES ('"
            + id
            + "',0,'"
            + UUID.randomUUID()
            + "','2026-10-01T10:00:00Z','NOTE',NULL,NULL,'Preserved history')");
  }

  private static void verifyPreserved(Statement statement, UUID id) throws SQLException {
    try (var rows =
        statement.executeQuery(
            "SELECT id, version, notes, applied_on"
                + " FROM workshop_migration_check.job_applications")) {
      assertThat(rows.next()).isTrue();
      assertThat(rows.getObject(1, UUID.class)).isEqualTo(id);
      assertThat(rows.getLong(2)).isEqualTo(7);
      assertThat(rows.getString(3)).isEqualTo("Preserved note");
      assertThat(rows.getDate(4).toString()).isEqualTo("2026-10-01");
    }
    try (var rows =
        statement.executeQuery("SELECT body FROM workshop_migration_check.application_timeline")) {
      rows.next();
      assertThat(rows.getString(1)).isEqualTo("Preserved history");
    }
    try (var rows =
        statement.executeQuery("SELECT count(*) FROM workshop_migration_check.application_tasks")) {
      rows.next();
      assertThat(rows.getInt(1)).isEqualTo(1);
    }
  }
}
