package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("cloud")
@TestPropertySource(properties = "server.tomcat.remoteip.internal-proxies=127[.]0[.]0[.]1")
class CloudDeploymentIT extends IntegrationTest {
    @Test
    void probesArePublicAndDoNotDiscloseHealthComponents() throws Exception
    {
        for (String probe : new String[]{"liveness", "readiness"}) {
            mvc
                .perform(get("/actuator/health/" + probe))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("components"))));
        }
    }

    @Test
    void trustedProxyHttpsCreatesASecureSessionAndIgnoresForwardedHost() throws Exception
    {
        HttpRequest request = HttpRequest
            .newBuilder(URI.create("http://127.0.0.1:" + port + "/applications"))
            .header("X-Forwarded-Proto", "https")
            .header("X-Forwarded-Host", "tracker.example.test")
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location")).contains("https://127.0.0.1/login");
        assertThat(response.headers().allValues("set-cookie")).anySatisfy(cookie -> {
            assertThat(cookie).contains("JSESSIONID=", "Secure", "HttpOnly", "SameSite=Strict");
        });
    }
}
