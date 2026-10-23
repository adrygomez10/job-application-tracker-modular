package dev.adriangm.tracker.exports.api;

import java.time.Instant;
import java.util.List;

/** Immutable input for plugins. Business and storage models remain outside the plugin contract. */
public record ExportData(
    String dataset, List<String> columns, List<List<String>> rows, Instant generatedAt) {
  public ExportData {
    columns = List.copyOf(columns);
    rows = rows.stream().map(List::copyOf).toList();
  }
}
