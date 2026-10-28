package dev.adriangm.tracker.imports.application;

import java.util.LinkedHashMap;
import java.util.Locale;

public final class NormalizeFields implements RowFilter {
    @Override
    public FilterResult apply(ImportRow row)
    {
        var fields = new LinkedHashMap<String, String>();
        row.fields().forEach((name, value) -> fields.put(name, value.strip()));
        fields.compute("source", (name, value) -> value.toLowerCase(Locale.ROOT));
        fields.compute("work_mode", (name, value) -> mode(value));
        return FilterResult.accept(new ImportRow(row.number(), fields));
    }

    private static String mode(String value)
    {
        return value == null || value.isBlank() ? "UNSPECIFIED" : value.toUpperCase(Locale.ROOT);
    }
}
