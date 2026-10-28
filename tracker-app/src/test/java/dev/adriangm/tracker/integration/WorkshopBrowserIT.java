package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class WorkshopBrowserIT extends IntegrationTest {
    @Test
    void importDownloadsAndReadPagesWorkOnDesktopAndMobile() throws IOException
    {
        try (
            var playwright = Playwright.create();
            var browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            var page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1440, 960))
        ) {
            page.navigate("http://localhost:" + port + "/login");
            page.getByLabel("Usuario").fill(USER);
            page.getByLabel("Contraseña").fill(PASSWORD);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Entrar")).click();
            page.navigate("http://localhost:" + port + "/imports");
            tryTemplate(page);
            page.navigate("http://localhost:" + port + "/imports");
            page
                .getByLabel("Archivo CSV UTF-8")
                .setInputFiles(
                    new FilePayload(
                        "offers.csv",
                        "text/csv",
                        "source,external_id,company,role\ndemo,1,Workshop Example,Java Developer\n"
                            .getBytes(StandardCharsets.UTF_8)
                    )
                );
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Previsualizar")).click();
            assertThat(page.locator("main").innerText()).contains("NEW", "Workshop Example");
            capture(page, "import-preview");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirmar filas válidas")).click();
            assertThat(page.locator("main").innerText()).contains("IMPORTED");
            downloadsAndReadPages(page);
        }
    }

    private void tryTemplate(Page page) throws IOException
    {
        var download = page
            .waitForDownload(
                () -> page
                    .getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Descargar plantilla CSV de prueba"))
                    .click()
            );
        assertThat(download.suggestedFilename()).isEqualTo("offers-template.csv");
        page
            .getByLabel("Archivo CSV UTF-8")
            .setInputFiles(
                new FilePayload(download.suggestedFilename(), "text/csv", Files.readAllBytes(download.path()))
            );
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Previsualizar")).click();
        assertThat(page.locator("main").innerText()).contains("NEW", "Example, Labs", "Example Studio");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM job_applications", Long.class)).isZero();
        capture(page, "import-template");
    }

    private void downloadsAndReadPages(Page page)
    {
        page.navigate("http://localhost:" + port + "/exports");
        capture(page, "exports");
        var download = page
            .waitForDownload(
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Descargar")).click()
            );
        assertThat(download.suggestedFilename()).isEqualTo("applications.csv");
        for (String route : java.util.List.of("reminders", "insights")) {
            page.navigate("http://localhost:" + port + "/" + route);
            capture(page, route);
        }
    }

    private static void capture(Page page, String name)
    {
        for (int width : new int[]{1440, 390}) {
            page.setViewportSize(width, 960);
            assertThat((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth")).isTrue();
            page
                .screenshot(
                    new Page.ScreenshotOptions()
                        .setPath(Path.of("target/screenshots/workshop-" + name + "-" + width + ".png"))
                        .setFullPage(true)
                );
        }
        page.setViewportSize(1440, 960);
    }
}
