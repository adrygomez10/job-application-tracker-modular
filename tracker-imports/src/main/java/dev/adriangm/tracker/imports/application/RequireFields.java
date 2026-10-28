package dev.adriangm.tracker.imports.application;

import java.util.List;

public final class RequireFields implements RowFilter {
    @Override
    public FilterResult apply(ImportRow row)
    {
        for (String field : List.of("source", "external_id", "company", "role")) {
            if (row.field(field).isBlank()) {
                return FilterResult.reject(row, "Required field: " + field);
            }
        }
        if (row.field("external_id").length() > 180) {
            return FilterResult.reject(row, "External identifier is too long");
        }
        return FilterResult.accept(row);
    }
}
