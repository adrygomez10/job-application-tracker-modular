package dev.adriangm.tracker.imports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.applications.api.InvalidOffer;

public final class ValidateOffer implements RowFilter {
    private final ApplicationsApi applications;

    public ValidateOffer(ApplicationsApi applications)
    {
        this.applications = applications;
    }

    @Override
    public FilterResult apply(ImportRow row)
    {
        try {
            applications.validate(ImportMapping.offer(row));
            return FilterResult.accept(row);
        } catch (InvalidOffer invalid) {
            return FilterResult.reject(row, invalid.getMessage());
        }
    }
}
