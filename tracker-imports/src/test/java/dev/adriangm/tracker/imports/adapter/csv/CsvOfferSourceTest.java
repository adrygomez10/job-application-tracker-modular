package dev.adriangm.tracker.imports.adapter.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adriangm.tracker.imports.application.InvalidCsv;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CsvOfferSourceTest {
    private static final String HEADER = "source,external_id,company,role";
    private final CsvOfferSource source = new CsvOfferSource();

    @Test
    void parsesQuotedCommaAndMultilineField()
    {
        var rows = source.read(bytes(HEADER + "\r\ndemo,1,\"Example, Inc\",\"Java\nDeveloper\"\r\n"));
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().field("company")).isEqualTo("Example, Inc");
        assertThat(rows.getFirst().field("role")).isEqualTo("Java\nDeveloper");
    }

    @Test
    void rejectsMalformedFileBeforeReturningAnyRows()
    {
        assertThatThrownBy(() -> source.read(bytes(HEADER + "\ndemo,1,Example,Java\ndemo,2,\"unfinished")))
            .isInstanceOf(InvalidCsv.class);
    }

    @Test
    void rejectsInvalidUtf8AndOversizedFiles()
    {
        assertThatThrownBy(() -> source.read(new byte[]{(byte) 0xc3, (byte) 0x28})).isInstanceOf(InvalidCsv.class);
        assertThatThrownBy(() -> source.read(new byte[CsvOfferSource.MAX_BYTES + 1])).isInstanceOf(InvalidCsv.class);
    }

    @Test
    void rejectsUnknownOrMissingHeaders()
    {
        assertThatThrownBy(() -> source.read(bytes("company,role\nExample,Java"))).isInstanceOf(InvalidCsv.class);
        assertThatThrownBy(() -> source.read(bytes("source,external_id,company,role,unknown\n")))
            .isInstanceOf(InvalidCsv.class);
    }

    @Test
    void rejectsMoreThanOneThousandRecords()
    {
        byte[] contents = bytes(HEADER + "\n" + "demo,1,Example,Java\n".repeat(1001));
        assertThatThrownBy(() -> source.read(contents)).isInstanceOf(InvalidCsv.class);
    }

    private static byte[] bytes(String value)
    {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
