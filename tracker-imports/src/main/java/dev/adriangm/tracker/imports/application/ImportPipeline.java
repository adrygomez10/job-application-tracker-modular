package dev.adriangm.tracker.imports.application;

import java.util.List;

public final class ImportPipeline {
    private final List<RowFilter> filters;

    public ImportPipeline(List<RowFilter> filters)
    {
        this.filters = List.copyOf(filters);
    }

    public FilterResult apply(ImportRow original)
    {
        FilterResult result = FilterResult.accept(original);
        for (RowFilter filter : filters) {
            result = filter.apply(result.row());
            if (!result.accepted()) {
                return result;
            }
        }
        return result;
    }
}
