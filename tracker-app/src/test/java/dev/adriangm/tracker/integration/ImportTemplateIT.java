package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.adriangm.tracker.imports.application.ImportReport;
import dev.adriangm.tracker.imports.application.ImportService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ImportTemplateIT extends IntegrationTest {
  @Autowired ImportService imports;

  @Test
  void templateRequiresLoginAndIsLinkedFromImportPage() throws Exception {
    mvc.perform(get("/imports/template")).andExpect(status().is3xxRedirection());
    var page = mvc.perform(get("/imports").with(user(USER))).andReturn().getResponse();
    assertThat(page.getStatus()).isEqualTo(200);
    assertThat(page.getContentAsString())
        .contains("href=\"/imports/template\"", "Descargar plantilla CSV de prueba");
    assertThat(jdbc.queryForObject("SELECT count(*) FROM job_applications", Long.class)).isZero();
  }

  @Test
  void downloadedTemplateCanBeImportedAndExtendedWithANewRecord() throws Exception {
    byte[] original = download();
    assertThat(imports.preview(original).rows())
        .extracting(ImportReport.Row::outcome)
        .containsExactly("NEW", "NEW");
    assertThat(imports.confirm(original).rows())
        .extracting(ImportReport.Row::outcome)
        .containsExactly("IMPORTED", "IMPORTED");
    assertThat(
            jdbc.queryForObject(
                "SELECT location FROM job_applications WHERE company = ?",
                String.class,
                "Example, Labs"))
        .isEqualTo("Málaga");
    assertThat(
            jdbc.queryForObject(
                "SELECT work_mode FROM job_applications WHERE company = ?",
                String.class,
                "Example Studio"))
        .isEqualTo("UNSPECIFIED");
    byte[] extended =
        (new String(original, StandardCharsets.UTF_8)
                + "demo-template,template-003,Another Example,Platform Developer,"
                + "https://example.test/jobs/template-003,Madrid,HYBRID,\"Java, PostgreSQL\"\r\n")
            .getBytes(StandardCharsets.UTF_8);
    assertThat(imports.preview(extended).rows())
        .extracting(ImportReport.Row::outcome)
        .containsExactly("DUPLICATE", "DUPLICATE", "NEW");
    assertThat(imports.confirm(extended).rows())
        .extracting(ImportReport.Row::outcome)
        .containsExactly("DUPLICATE", "DUPLICATE", "IMPORTED");
    assertThat(jdbc.queryForObject("SELECT count(*) FROM job_applications", Long.class))
        .isEqualTo(3);
  }

  private byte[] download() throws Exception {
    var response = mvc.perform(get("/imports/template").with(user(USER))).andReturn().getResponse();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentType()).isEqualTo("text/csv;charset=UTF-8");
    assertThat(response.getHeader("Content-Disposition"))
        .contains("attachment", "offers-template.csv");
    assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    return response.getContentAsByteArray();
  }
}
