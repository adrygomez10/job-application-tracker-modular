package dev.adriangm.tracker.exports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Filter;
import dev.adriangm.tracker.exports.api.ExportDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExportService {
    private final ExportRegistry registry;
    private final Map<String, DatasetSource> sources;

    public ExportService(ExportRegistry registry, List<DatasetSource> sources)
    {
        this.registry = registry;
        this.sources = sources
            .stream()
            .collect(Collectors.toUnmodifiableMap(DatasetSource::dataset, Function.identity()));
    }

    public List<ExportDescriptor> formats()
    {
        return registry.descriptors();
    }

    public Download export(String id, Filter filter, Instant from, Instant until) throws IOException
    {
        var plugin = registry.require(id);
        DatasetSource source = sources.get(plugin.descriptor().dataset());
        if (source == null) {
            throw new IllegalArgumentException("Unsupported dataset");
        }
        var data = source.load(filter, from, until);
        var output = new ByteArrayOutputStream();
        plugin.write(data, output);
        return new Download(plugin.descriptor(), output.toByteArray());
    }

    public record Download(ExportDescriptor descriptor, byte[] contents) {
        public Download
        {
            contents = contents.clone();
        }

        @Override
        public byte[] contents()
        {
            return contents.clone();
        }
    }
}
