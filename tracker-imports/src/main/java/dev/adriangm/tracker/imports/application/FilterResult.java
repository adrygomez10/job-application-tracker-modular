package dev.adriangm.tracker.imports.application;

public record FilterResult(ImportRow row, String reason) {
    public static FilterResult accept(ImportRow row)
    {
        return new FilterResult(row, "");
    }

    public static FilterResult reject(ImportRow row, String reason)
    {
        return new FilterResult(row, reason);
    }

    public boolean accepted()
    {
        return reason.isEmpty();
    }
}
