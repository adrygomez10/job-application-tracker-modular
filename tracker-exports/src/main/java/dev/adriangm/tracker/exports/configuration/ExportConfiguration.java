package dev.adriangm.tracker.exports.configuration;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import dev.adriangm.tracker.exports.application.ApplicationsDataset;
import dev.adriangm.tracker.exports.application.ExportRegistry;
import dev.adriangm.tracker.exports.application.ExportService;
import dev.adriangm.tracker.exports.application.InterviewsDataset;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportConfiguration {
  @Bean
  ExportService exportService(
      List<ExportPlugin> plugins, ApplicationsApi applications, Clock clock) {
    return new ExportService(
        new ExportRegistry(plugins),
        List.of(
            new ApplicationsDataset(applications, clock),
            new InterviewsDataset(applications, clock)));
  }
}
