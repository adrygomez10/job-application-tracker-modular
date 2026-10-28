package dev.adriangm.tracker.integration;

import static dev.adriangm.tracker.support.Fixtures.TODAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.adriangm.tracker.applications.application.activities.AddNote;
import dev.adriangm.tracker.applications.application.details.RegisterApplication;
import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.TimelineEvent;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class WebIT extends IntegrationTest {
    @Autowired
    RegisterApplication register;
    @Autowired
    AddNote addNote;

    @Test
    void privatePagesRequireLoginButHealthDoesNot() throws Exception
    {
        mvc.perform(get("/applications")).andExpect(status().is3xxRedirection());
        mvc
            .perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("components"))));
        mvc.perform(get("/actuator/metrics")).andExpect(status().is3xxRedirection());
    }

    @Test
    void csrfIsRequiredForWrites() throws Exception
    {
        mvc.perform(post("/applications").with(user(USER))).andExpect(status().isForbidden());
    }

    @Test
    void invalidCreateKeepsFormValues() throws Exception
    {
        mvc
            .perform(
                write("/applications")
                    .param("id", UUID.randomUUID().toString())
                    .param("company", "Example")
                    .param("role", "")
                    .param("workMode", "REMOTE")
                    .param("status", "SAVED")
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Example")));
    }

    @Test
    void unknownRecordIs404AndStaleEditIs409() throws Exception
    {
        mvc.perform(get("/applications/" + UUID.randomUUID()).with(user(USER))).andExpect(status().isNotFound());
        var application = seed();
        addNote.execute(application.id(), 0, "New note");
        mvc
            .perform(
                write("/applications/" + application.id() + "/status")
                    .param("version", "0")
                    .param("status", "APPLIED")
                    .param("appliedOn", TODAY.toString())
            )
            .andExpect(status().isConflict());
    }

    @Test
    void invalidFiltersAre400() throws Exception
    {
        mvc
            .perform(
                get("/applications")
                    .with(user(USER))
                    .param("from", TODAY.toString())
                    .param("to", TODAY.minusDays(1).toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void renderingEscapesUserSuppliedHtml() throws Exception
    {
        var details = new ApplicationDetails(
            "<script>alert(1)</script>",
            "Developer",
            "",
            "",
            "",
            WorkMode.REMOTE,
            "",
            "",
            ""
        );
        var application = register.execute(UUID.randomUUID(), details, Status.SAVED, null);
        mvc
            .perform(get("/applications/" + application.id()).with(user(USER)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("&lt;script&gt;")))
            .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void invalidAndAmbiguousTimesAreRejected() throws Exception
    {
        var application = seed();
        for (String time : List.of("2026-03-29T02:30", "2026-10-25T02:30")) {
            mvc
                .perform(
                    write("/applications/" + application.id() + "/interviews")
                        .param("version", "0")
                        .param("title", "Interview")
                        .param("kind", "INTERVIEW")
                        .param("startsAt", time)
                )
                .andExpect(status().isBadRequest());
        }
        assertThat(read(application.id()).interviews()).isEmpty();
    }

    @Test
    void editFormUpdatesDetailsWithoutChangingState() throws Exception
    {
        var application = seed();
        mvc.perform(get("/applications/" + application.id() + "/edit").with(user(USER))).andExpect(status().isOk());
        mvc
            .perform(
                write("/applications/" + application.id() + "/edit")
                    .param("id", application.id().toString())
                    .param("version", "0")
                    .param("company", "Updated Labs")
                    .param("role", "Senior Java Developer")
                    .param("workMode", "REMOTE")
                    .param("status", "SAVED")
            )
            .andExpect(status().is3xxRedirection());
        var changed = read(application.id());
        assertThat(changed.details().company()).isEqualTo("Updated Labs");
        assertThat(changed.status()).isEqualTo(Status.SAVED);
        assertThat(changed.timeline().getLast().kind()).isEqualTo(TimelineEvent.DETAILS_UPDATED);
    }

    @Test
    void archiveAndRestoreKeepAllHistory() throws Exception
    {
        var application = seed();
        mvc
            .perform(write("/applications/" + application.id() + "/archive").param("version", "0"))
            .andExpect(status().is3xxRedirection());
        assertThat(read(application.id()).archived()).isTrue();
        mvc
            .perform(write("/applications/" + application.id() + "/restore").param("version", "1"))
            .andExpect(status().is3xxRedirection());
        var restored = read(application.id());
        assertThat(restored.archived()).isFalse();
        assertThat(restored.timeline())
            .extracting(entry -> entry.kind())
            .containsExactly(TimelineEvent.CREATED, TimelineEvent.ARCHIVED, TimelineEvent.UNARCHIVED);
    }

    @Test
    void wrongPasswordIsRejectedAndMetricsRequireAuthenticatedAccess() throws Exception
    {
        mvc
            .perform(post("/login").with(csrf()).param("username", USER).param("password", "wrong-password"))
            .andExpect(redirectedUrl("/login?error"));
        mvc.perform(get("/actuator/metrics").with(user(USER))).andExpect(status().isOk());
    }

    private static MockHttpServletRequestBuilder write(String path)
    {
        return post(path).with(user(USER)).with(csrf());
    }
}
