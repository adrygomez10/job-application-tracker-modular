package dev.adriangm.tracker.imports.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.imports.adapter.csv.CsvOfferSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ImportServiceTest {
    private static final String HEADER = "source,external_id,company,role\n";

    @Test
    void previewNeverSavesAndClassifiesRepeatedKeys()
    {
        var writes = new ArrayList<ImportEntry>();
        var service = service((source, id) -> Optional.empty(), entry -> {
            writes.add(entry);
            return "IMPORTED";
        });
        var report = service
            .preview(csv("demo,1,Example,Java\ndemo,1,Example,Java\ndemo,1,Other,Java\ndemo,2,,Java\n"));
        assertThat(report.rows())
            .extracting(ImportReport.Row::outcome)
            .containsExactly("NEW", "DUPLICATE", "CONFLICT", "REJECTED");
        assertThat(writes).isEmpty();
    }

    @Test
    void confirmationKeepsSuccessfulRowsAndStopsAfterStorageFailure()
    {
        var saved = new ArrayList<String>();
        var service = service((source, id) -> Optional.empty(), entry -> {
            if (entry.externalId().equals("2")) {
                throw new ImportUnavailable(new IllegalStateException("Test failure"));
            }
            saved.add(entry.externalId());
            return "IMPORTED";
        });
        var report = service.confirm(csv("demo,1,Example,Java\ndemo,2,Example,Java\ndemo,3,Example,Java\n"));
        assertThat(report.rows())
            .extracting(ImportReport.Row::outcome)
            .containsExactly("IMPORTED", "PENDING", "PENDING");
        assertThat(report.stopped()).isTrue();
        assertThat(saved).containsExactly("1");
    }

    private static ImportService service(ImportLedger ledger, OfferWriter writer)
    {
        return new ImportService(
            new CsvOfferSource(),
            new ImportPipeline(List.of(new NormalizeFields(), new RequireFields())),
            ledger,
            writer
        );
    }

    private static byte[] csv(String rows)
    {
        return (HEADER + rows).getBytes(StandardCharsets.UTF_8);
    }
}
