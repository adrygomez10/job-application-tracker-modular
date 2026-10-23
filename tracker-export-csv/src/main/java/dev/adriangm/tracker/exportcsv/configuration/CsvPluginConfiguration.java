package dev.adriangm.tracker.exportcsv.configuration;

import dev.adriangm.tracker.exportcsv.CsvExportPlugin;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvPluginConfiguration {
  @Bean
  ExportPlugin csvExportPlugin() {
    return new CsvExportPlugin();
  }
}
