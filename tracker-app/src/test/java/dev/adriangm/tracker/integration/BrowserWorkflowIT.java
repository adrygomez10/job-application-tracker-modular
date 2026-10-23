package dev.adriangm.tracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

/** Walks through the daily workflow in a real browser and saves screenshots of every screen. */
class BrowserWorkflowIT extends IntegrationTest {
  private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

  @Value("${tracker.zone}")
  String configuredZone;

  @Test
  void browserCanCompleteTheDailyWorkflowOnDesktop() {
    assertThat(configuredZone).isEqualTo(ZONE.getId());
    try (var playwright = Playwright.create();
        var browser =
            playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        var page =
            browser.newPage(
                new Browser.NewPageOptions()
                    .setViewportSize(1440, 960)
                    .setLocale("es-ES")
                    .setTimezoneId(ZONE.getId()))) {
      login(page);
      createApplicationInBrowser(page);
      submitApplicationInBrowser(page);
      scheduleInterviewInBrowser(page);
      completeTaskInBrowser(page);
      addHistoryNoteInBrowser(page);
      reviewDetailAndAgenda(page);
      filterApplicationsInBrowser(page);
    }
  }

  private void createApplicationInBrowser(Page page) {
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Guardar oferta")).click();
    captureResponsive(page, "new-application");
    page.getByLabel("Empresa *").fill("Workshop Labs");
    page.getByLabel("Puesto *").fill("Java developer");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Guardar").setExact(true))
        .click();
    assertThat(page.locator("h1").innerText()).isEqualTo("Java developer");
  }

  private void submitApplicationInBrowser(Page page) {
    var statusForm =
        page.getByRole(AriaRole.FORM, new Page.GetByRoleOptions().setName("Actualizar estado"));
    statusForm
        .getByLabel("Estado", new Locator.GetByLabelOptions().setExact(true))
        .selectOption("APPLIED");
    statusForm.getByLabel("Fecha de solicitud").fill(LocalDate.now(ZONE).toString());
    statusForm.getByRole(AriaRole.BUTTON).click();
  }

  private void scheduleInterviewInBrowser(Page page) {
    page.locator("summary")
        .filter(new Locator.FilterOptions().setHasText("Añadir entrevista o prueba"))
        .click();
    var interview =
        page.getByRole(
            AriaRole.FORM, new Page.GetByRoleOptions().setName("Añadir entrevista").setExact(true));
    interview.getByLabel("Título").fill("Technical interview");
    interview
        .getByLabel("Fecha y hora (" + ZONE.getId() + ")")
        .fill(LocalDate.now(ZONE).plusDays(2) + "T10:30");
    interview.getByRole(AriaRole.BUTTON).click();
  }

  private void completeTaskInBrowser(Page page) {
    page.locator("summary").filter(new Locator.FilterOptions().setHasText("Añadir tarea")).click();
    var task =
        page.getByRole(
            AriaRole.FORM, new Page.GetByRoleOptions().setName("Añadir tarea").setExact(true));
    task.getByLabel("Tarea", new Locator.GetByLabelOptions().setExact(true))
        .fill("Prepare interview");
    task.getByLabel("Vencimiento").fill(LocalDate.now(ZONE).plusDays(1).toString());
    task.getByRole(AriaRole.BUTTON).click();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Completar tarea")).click();
    assertThat(page.getByText("Completada", new Page.GetByTextOptions().setExact(true)).count())
        .isEqualTo(1);
  }

  private void addHistoryNoteInBrowser(Page page) {
    page.getByLabel("Nueva nota").fill("Prepare portfolio examples");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Añadir nota")).click();
    assertThat(page.locator(".timeline").innerText())
        .contains("Prepare portfolio examples", "Enviada");
  }

  private void reviewDetailAndAgenda(Page page) {
    captureResponsive(page, "detail");
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Editar datos")).click();
    captureResponsive(page, "edit-application");
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Cancelar").setExact(true))
        .click();
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Agenda").setExact(true))
        .click();
    assertThat(page.locator("main").innerText())
        .contains("Technical interview")
        .doesNotContain("Prepare interview");
    captureResponsive(page, "agenda");
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Resumen").setExact(true))
        .click();
    captureResponsive(page, "dashboard");
  }

  private void filterApplicationsInBrowser(Page page) {
    String background =
        page.locator("body").evaluate("e => getComputedStyle(e).backgroundColor").toString();
    page.getByRole(
            AriaRole.LINK, new Page.GetByRoleOptions().setName("Candidaturas").setExact(true))
        .click();
    PlaywrightAssertions.assertThat(page.locator("body")).hasCSS("background-color", background);
    PlaywrightAssertions.assertThat(page.locator(".nav-link[aria-current='page']"))
        .containsText("Candidaturas");
    page.getByLabel("Empresa o puesto").fill("Workshop");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filtrar")).click();
    assertThat(page.locator(".application-list").innerText()).contains("Java developer");
    captureResponsive(page, "list");
  }

  private void login(Page page) {
    page.navigate("http://127.0.0.1:" + port + "/");
    captureResponsive(page, "login");
    page.getByLabel("Usuario").fill(USER);
    page.getByLabel("Contraseña").fill(PASSWORD);
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Entrar")).click();
    assertThat(page.locator("h1").innerText()).isEqualTo("Resumen");
  }

  private void captureResponsive(Page page, String name) {
    screenshot(page, "desktop-" + name);
    assertThat((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth"))
        .as(name + " desktop overflow")
        .isTrue();
    page.setViewportSize(390, 844);
    screenshot(page, "mobile-" + name);
    assertThat((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth"))
        .as(name + " mobile overflow")
        .isTrue();
    page.setViewportSize(1440, 960);
  }

  private void screenshot(Page page, String name) {
    page.waitForLoadState(LoadState.LOAD);
    page.evaluate("() => document.fonts.ready.then(() => true)");
    page.screenshot(
        new Page.ScreenshotOptions()
            .setPath(Path.of("target/screenshots/" + name + ".png"))
            .setFullPage(true));
  }
}
