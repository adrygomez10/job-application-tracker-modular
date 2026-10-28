package dev.adriangm.tracker.imports.configuration;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.imports.adapter.csv.CsvOfferSource;
import dev.adriangm.tracker.imports.application.ImportLedger;
import dev.adriangm.tracker.imports.application.ImportPipeline;
import dev.adriangm.tracker.imports.application.ImportService;
import dev.adriangm.tracker.imports.application.NormalizeFields;
import dev.adriangm.tracker.imports.application.OfferWriter;
import dev.adriangm.tracker.imports.application.RequireFields;
import dev.adriangm.tracker.imports.application.ValidateOffer;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportConfiguration {
    @Bean
    ImportService importService(ApplicationsApi applications, ImportLedger ledger, OfferWriter writer)
    {
        var pipeline = new ImportPipeline(
            List.of(new NormalizeFields(), new RequireFields(), new ValidateOffer(applications))
        );
        return new ImportService(new CsvOfferSource(), pipeline, ledger, writer);
    }
}
