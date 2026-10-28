package dev.adriangm.tracker.exports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Filter;
import dev.adriangm.tracker.exports.api.ExportData;
import java.time.Instant;

public interface DatasetSource {
    String dataset();

    ExportData load(Filter filter, Instant from, Instant until);
}
