package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.adriangm.tracker.imports.application.ImportReport;
import dev.adriangm.tracker.imports.application.ImportService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

class ImportPersistenceIT extends IntegrationTest {
    @Autowired
    ImportService imports;
    private static final String HEADER = "source,external_id,company,role\n";

    @Test
    void previewAndReimportNeverOverwritePersonalChanges()
    {
        byte[] file = csv("demo,1,Example,Developer\n");
        assertThat(imports.preview(file).rows().getFirst().outcome()).isEqualTo("NEW");
        assertThat(totalApplications()).isZero();
        assertThat(imports.confirm(file).rows().getFirst().outcome()).isEqualTo("IMPORTED");
        jdbc.update("UPDATE job_applications SET company = 'Personal edit', notes = 'Keep me'");
        assertThat(imports.confirm(file).rows().getFirst().outcome()).isEqualTo("DUPLICATE");
        assertThat(imports.confirm(csv("demo,1,Changed,Developer\n")).rows().getFirst().outcome())
            .isEqualTo("CONFLICT");
        assertThat(jdbc.queryForObject("SELECT notes FROM job_applications", String.class)).isEqualTo("Keep me");
        assertThat(totalApplications()).isEqualTo(1);
    }

    @Test
    void concurrentConfirmationsCreateExactlyOneApplication() throws Exception
    {
        byte[] file = csv("demo,1,Example,Developer\n");
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<String> confirm = () -> imports.confirm(file).rows().getFirst().outcome();
            List<Future<String>> results = executor.invokeAll(List.of(confirm, confirm));
            assertThat(List.of(results.get(0).get(), results.get(1).get()))
                .containsExactlyInAnyOrder("IMPORTED", "DUPLICATE");
        }
        assertThat(totalApplications()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM imported_offers", Long.class)).isEqualTo(1);
    }

    @Test
    void failedApplicationInsertRollsBackLedgerAndStopsTheBatch()
    {
        jdbc.execute("ALTER TABLE job_applications ADD CONSTRAINT test_import_failure" + " CHECK (company <> 'Fail')");
        try {
            ImportReport report = imports
                .confirm(csv("demo,1,Example,Developer\ndemo,2,Fail,Developer\ndemo,3,Later,Developer\n"));
            assertThat(report.rows())
                .extracting(ImportReport.Row::outcome)
                .containsExactly("IMPORTED", "PENDING", "PENDING");
            assertThat(jdbc.queryForObject("SELECT count(*) FROM imported_offers", Long.class)).isEqualTo(1);
            assertThat(totalApplications()).isEqualTo(1);
        } finally {
            jdbc.execute("ALTER TABLE job_applications DROP CONSTRAINT test_import_failure");
        }
    }

    @Test
    void confirmationRequiresSessionPreviewAndCsrf() throws Exception
    {
        mvc.perform(post("/imports/confirm").with(user(USER))).andExpect(status().isForbidden());
        mvc.perform(post("/imports/confirm").with(user(USER)).with(csrf())).andExpect(status().isBadRequest());
        var file = new MockMultipartFile("file", "offers.csv", "text/csv", csv("demo,1,Example,Developer\n"));
        var session = new MockHttpSession();
        mvc
            .perform(multipart("/imports/preview").file(file).session(session).with(user(USER)).with(csrf()))
            .andExpect(status().isOk());
        assertThat(totalApplications()).isZero();
        mvc.perform(post("/imports/confirm").session(session).with(user(USER)).with(csrf())).andExpect(status().isOk());
        assertThat(totalApplications()).isEqualTo(1);
    }

    private long totalApplications()
    {
        return jdbc.queryForObject("SELECT count(*) FROM job_applications", Long.class);
    }

    private static byte[] csv(String rows)
    {
        return (HEADER + rows).getBytes(StandardCharsets.UTF_8);
    }
}
