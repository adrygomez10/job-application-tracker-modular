package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;

class ExportWebIT extends IntegrationTest {
  @Test
  void emptyOptionalFieldsAndUncheckedArchivedCheckboxAllowDownload() throws Exception {
    var result =
        mvc.perform(
                get("/exports/download")
                    .param("plugin", "applications-csv")
                    .param("q", "")
                    .param("status", "")
                    .param("mode", "")
                    .param("from", "")
                    .param("to", "")
                    .with(user(USER)))
            .andReturn();
    assertThat(result.getResolvedException()).isNull();
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    assertThat(result.getResponse().getHeader("Content-Disposition")).contains("applications.csv");
  }

  @Test
  void unknownPluginAndReversedDatesAreRejected() throws Exception {
    var unknown =
        mvc.perform(get("/exports/download").param("plugin", "unknown").with(user(USER)))
            .andReturn();
    assertThat(unknown.getResponse().getStatus()).isEqualTo(400);
    var reversed =
        mvc.perform(
                get("/exports/download")
                    .param("plugin", "interviews-ics")
                    .param("from", "2026-11-05")
                    .param("to", "2026-11-01")
                    .with(user(USER)))
            .andReturn();
    assertThat(reversed.getResponse().getStatus()).isEqualTo(400);
  }
}
