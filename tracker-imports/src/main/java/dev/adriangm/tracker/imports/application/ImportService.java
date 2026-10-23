package dev.adriangm.tracker.imports.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ImportService {
  private final CsvSource source;
  private final ImportPipeline pipeline;
  private final ImportLedger ledger;
  private final OfferWriter writer;

  public ImportService(
      CsvSource source, ImportPipeline pipeline, ImportLedger ledger, OfferWriter writer) {
    this.source = source;
    this.pipeline = pipeline;
    this.ledger = ledger;
    this.writer = writer;
  }

  public ImportReport preview(byte[] contents) {
    return process(contents, false);
  }

  public ImportReport confirm(byte[] contents) {
    return process(contents, true);
  }

  private ImportReport process(byte[] contents, boolean persist) {
    var rows = source.read(contents);
    var report = new ArrayList<ImportReport.Row>();
    var seen = new HashMap<String, String>();
    boolean stopped = false;
    for (ImportRow raw : rows) {
      if (stopped) {
        report.add(result(raw, "PENDING", "Previous storage failure"));
        continue;
      }
      FilterResult checked = pipeline.apply(raw);
      if (!checked.accepted()) {
        report.add(result(checked.row(), "REJECTED", checked.reason()));
        continue;
      }
      try {
        report.add(classify(checked.row(), seen, persist));
      } catch (ImportUnavailable unavailable) {
        report.add(result(checked.row(), "PENDING", "Storage failure; review and preview again"));
        stopped = true;
      }
    }
    return new ImportReport(report, stopped);
  }

  private ImportReport.Row classify(ImportRow row, Map<String, String> seen, boolean persist) {
    ImportEntry entry = ImportMapping.entry(row);
    String key = entry.source().length() + ":" + entry.source() + entry.externalId();
    String previous = seen.putIfAbsent(key, entry.fingerprint());
    if (previous != null) {
      return result(row, outcome(previous, entry), "Repeated key in file");
    }
    Optional<String> stored = ledger.fingerprint(entry.source(), entry.externalId());
    if (stored.isPresent()) {
      return result(row, outcome(stored.get(), entry), "Previously imported key");
    }
    return result(row, persist ? writer.save(entry) : "NEW", "");
  }

  private static String outcome(String previous, ImportEntry entry) {
    return previous.equals(entry.fingerprint()) ? "DUPLICATE" : "CONFLICT";
  }

  private static ImportReport.Row result(ImportRow row, String outcome, String reason) {
    return new ImportReport.Row(
        row.number(), row.field("company"), row.field("role"), outcome, reason);
  }
}
