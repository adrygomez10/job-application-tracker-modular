package dev.adriangm.tracker.imports.application;

import java.util.Map;

public record ImportRow(int number, Map<String, String> fields) {
    public ImportRow
    {
        fields = Map.copyOf(fields);
    }

    public String field(String name)
    {
        return fields.getOrDefault(name, "");
    }
}
