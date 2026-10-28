package dev.adriangm.tracker.imports.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.imports.adapter.csv.CsvOfferSource;
import dev.adriangm.tracker.imports.application.ImportPipeline;
import dev.adriangm.tracker.imports.application.ImportService;
import dev.adriangm.tracker.imports.application.NormalizeFields;
import dev.adriangm.tracker.imports.application.RequireFields;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;

class PreviewExpiryTest {
    @Test
    void expiredPreviewCannotBeConfirmed() throws Exception
    {
        var imports = new ImportService(
            new CsvOfferSource(),
            new ImportPipeline(List.of(new NormalizeFields(), new RequireFields())),
            (source, id) -> Optional.empty(),
            entry -> {
                throw new AssertionError("Expired preview must not write");
            }
        );
        var now = Instant.parse("2026-10-23T10:00:00Z");
        var session = new MockHttpSession();
        var file = new MockMultipartFile(
            "file",
            "offers.csv",
            "text/csv",
            "source,external_id,company,role\ndemo,1,Example,Java\n".getBytes(StandardCharsets.UTF_8)
        );
        new ImportController(imports, Clock.fixed(now, ZoneOffset.UTC))
            .preview(file, session, new ExtendedModelMap(), new MockHttpServletResponse());
        var response = new MockHttpServletResponse();
        new ImportController(imports, Clock.fixed(now.plusSeconds(900), ZoneOffset.UTC))
            .confirm(session, new ExtendedModelMap(), response);
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
