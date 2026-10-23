package dev.adriangm.tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModuleArchitectureTest {
  private static final com.tngtech.archunit.core.domain.JavaClasses CLASSES =
      ArchitectureClasses.ALL;

  @Test
  void modulesAreAcyclic() {
    slices().matching("dev.adriangm.tracker.(*)..").should().beFreeOfCycles().check(CLASSES);
  }

  @Test
  void featuresOnlyUseApplicationsApi() {
    noClasses()
        .that()
        .resideOutsideOfPackages("..applications..", "..configuration..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..applications.domain..", "..applications.application..", "..applications.adapter..")
        .check(CLASSES);
  }

  @Test
  void exportersOnlyUseExportContracts() {
    noClasses()
        .that()
        .resideInAnyPackage("..exportcsv..", "..exportics..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..applications..",
            "..exports.application..",
            "..exports.adapter..",
            "jakarta.persistence..")
        .check(CLASSES);
  }

  @Test
  void newModulesAreIncludedInArchitectureChecks() {
    assertThat(CLASSES.stream().map(type -> type.getPackageName()).distinct())
        .anyMatch(name -> name.contains(".imports."))
        .anyMatch(name -> name.contains(".exports."))
        .anyMatch(name -> name.contains(".reminders."));
  }
}
