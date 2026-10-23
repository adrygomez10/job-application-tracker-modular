package dev.adriangm.tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import dev.adriangm.tracker.imports.fixture.IllegalModuleDependency;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoundaryDemonstrationTest {
  @TempDir Path temporary;

  @Test
  void compilerRejectsATrackerTypeOutsideTheClasspath() throws Exception {
    Path file = temporary.resolve("Forbidden.java");
    Files.writeString(
        file,
        "import dev.adriangm.tracker.applications.api.ApplicationsApi;"
            + " class Forbidden { ApplicationsApi applications; }");
    Path empty = Files.createDirectory(temporary.resolve("empty"));
    var errors = new ByteArrayOutputStream();
    int result =
        ToolProvider.getSystemJavaCompiler()
            .run(
                null,
                errors,
                errors,
                "-classpath",
                empty.toString(),
                "-d",
                temporary.toString(),
                file.toString());
    assertThat(result).isNotZero();
    assertThat(errors.toString()).contains("does not exist");
  }

  @Test
  void archUnitRejectsAccessibleButInternalTypes() {
    var fixture = new ClassFileImporter().importClasses(IllegalModuleDependency.class);
    var evaluation =
        noClasses()
            .that()
            .resideInAPackage("..imports..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..applications.domain..")
            .evaluate(fixture);
    assertThat(evaluation.hasViolation()).isTrue();
  }
}
