package dev.adriangm.tracker.exportics.configuration;

import dev.adriangm.tracker.exportics.IcsExportPlugin;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IcsPluginConfiguration {
  @Bean
  ExportPlugin icsExportPlugin() {
    return new IcsExportPlugin();
  }
}
