package dev.adriangm.tracker.exports.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adriangm.tracker.exports.api.ExportData;
import dev.adriangm.tracker.exports.api.ExportDescriptor;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExportRegistryTest {
    @Test
    void rejectsDuplicateRegistrationAndUnknownFormat()
    {
        var plugin = new TextPlugin();
        assertThatThrownBy(() -> new ExportRegistry(List.of(plugin, plugin)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExportRegistry(List.of(plugin)).require("missing"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extensionWorksWithoutChangingCore() throws IOException
    {
        var registry = new ExportRegistry(List.of(new TextPlugin()));
        var output = new ByteArrayOutputStream();
        registry
            .require("test-text")
            .write(
                new ExportData("applications", List.of("company"), List.of(List.of("Example")), Instant.EPOCH),
                output
            );
        assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo("Example");
    }

    private static final class TextPlugin implements ExportPlugin {
        @Override
        public ExportDescriptor descriptor()
        {
            return new ExportDescriptor("test-text", "applications", "TXT", "text/plain", "test.txt");
        }

        @Override
        public void write(ExportData data, OutputStream output) throws IOException
        {
            output.write(data.rows().getFirst().getFirst().getBytes(StandardCharsets.UTF_8));
        }
    }
}
