package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.exports.application.ExportService;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExportFormatsIT extends IntegrationTest {
  @Autowired ExportService exports;

  @Test
  void independentCalendarReaderAcceptsUnicodeEscapingAndStableIdentity() throws Exception {
    var application = seed();
    var id = UUID.randomUUID();
    jdbc.update(
        """
        INSERT INTO application_interviews VALUES
        (?, 0, ?, ?, 'INTERVIEW', TIMESTAMPTZ '2026-11-01 10:00:00Z', ?, '', '', 'SCHEDULED', '')
        """,
        application.id(),
        id,
        "Entrevista, Java; " + "á".repeat(75),
        "Madrid\nOnline");
    String first = calendar();
    var parsed = new CalendarBuilder().build(new StringReader(first));
    assertThat(parsed.getComponents()).hasSize(1);
    assertThat(first).contains("UID:" + id + "@tracker.local", "DTSTART:20261101T100000Z");
    assertThat(calendar()).contains("UID:" + id + "@tracker.local");
    assertThat(first).doesNotContain("DTEND");
    for (String line : first.split("\r\n")) {
      assertThat(line.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(75);
    }
  }

  @Test
  void independentCsvReaderRecoversQuotedUnicodeAndNewlines() throws Exception {
    seed();
    jdbc.update("UPDATE job_applications SET company = ?", "Example, Inc\nMálaga");
    var file =
        exports.export(
            "applications-csv",
            new ApplicationsApi.Filter("", "", "", null, null, false),
            Instant.EPOCH,
            Instant.now());
    try (var reader =
        new com.opencsv.CSVReader(
            new StringReader(new String(file.contents(), StandardCharsets.UTF_8)))) {
      var rows = reader.readAll();
      assertThat(rows).hasSize(2);
      assertThat(rows.get(1)[1]).isEqualTo("Example, Inc\nMálaga");
    }
  }

  private String calendar() throws Exception {
    var download =
        exports.export(
            "interviews-ics",
            new ApplicationsApi.Filter("", "", "", null, null, false),
            Instant.parse("2026-11-01T00:00:00Z"),
            Instant.parse("2026-11-02T00:00:00Z"));
    return new String(download.contents(), StandardCharsets.UTF_8);
  }
}
