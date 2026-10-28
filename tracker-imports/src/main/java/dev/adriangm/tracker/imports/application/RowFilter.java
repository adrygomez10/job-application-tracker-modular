package dev.adriangm.tracker.imports.application;

/** A filter transforms or rejects one row without writing outside the pipeline. */
public interface RowFilter {
    FilterResult apply(ImportRow row);
}
