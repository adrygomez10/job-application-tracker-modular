package dev.adriangm.tracker.imports.application;

import java.util.List;

public interface CsvSource {
    List<ImportRow> read(byte[] contents);
}
