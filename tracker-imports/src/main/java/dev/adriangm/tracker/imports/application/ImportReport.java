package dev.adriangm.tracker.imports.application;

import java.util.List;

public record ImportReport(List<Row> rows, boolean stopped) {
  public ImportReport {
    rows = List.copyOf(rows);
  }

  public record Row(int number, String company, String role, String outcome, String reason) {}
}
