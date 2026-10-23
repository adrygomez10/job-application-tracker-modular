package dev.adriangm.tracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

final class ArchitectureClasses {
  static final JavaClasses ALL =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .withImportOption(location -> !location.toString().contains("-tests.jar"))
          .importPackages("dev.adriangm.tracker");

  private ArchitectureClasses() {}
}
