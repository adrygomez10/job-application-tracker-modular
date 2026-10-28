package dev.adriangm.tracker.imports.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.imports.adapter.csv.CsvOfferSource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImportPipelineTest {
    @Test
    void normalizationRunsBeforeRequiredFields()
    {
        var row = new ImportRow(
            1,
            Map.of("source", " Demo ", "external_id", " 01 ", "company", "  ", "role", "Developer")
        );
        var pipeline = new ImportPipeline(List.of(new NormalizeFields(), new RequireFields()));
        var result = pipeline.apply(row);
        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).contains("company");
        assertThat(result.row().field("source")).isEqualTo("demo");
        assertThat(row.field("source")).isEqualTo(" Demo ");
    }

    @Test
    void optionalModeDefaultsWithoutChangingExternalIdentifierCase()
    {
        var result = new NormalizeFields().apply(new ImportRow(1, Map.of("source", "DEMO", "external_id", "AbC")));
        assertThat(result.row().field("work_mode")).isEqualTo("UNSPECIFIED");
        assertThat(result.row().field("external_id")).isEqualTo("AbC");
    }

    @Test
    void fingerprintsIgnoreWhitespaceButDetectChangedImportedFields()
    {
        var source = new CsvOfferSource();
        var normalize = new NormalizeFields();
        var first = normalize.apply(source.read(csv("Example,Developer")).getFirst()).row();
        var same = normalize.apply(source.read(csv(" Example ,Developer")).getFirst()).row();
        var changed = normalize.apply(source.read(csv("Other,Developer")).getFirst()).row();
        assertThat(ImportMapping.entry(first).fingerprint()).isEqualTo(ImportMapping.entry(same).fingerprint());
        assertThat(ImportMapping.entry(first).fingerprint()).isNotEqualTo(ImportMapping.entry(changed).fingerprint());
    }

    private static byte[] csv(String values)
    {
        return ("source,external_id,company,role\ndemo,1," + values + "\n").getBytes(StandardCharsets.UTF_8);
    }
}
