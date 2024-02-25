package de.birk.calory;

import static com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.DoNotIncludeTests;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class ArchUnitTest {

  private static final String BASE_PACKAGE = "de.birk.calory";
  private static final String MODULE_CONTROLLER = BASE_PACKAGE + ".adapter.primary";
  private static final String MODULE_DOMAIN = BASE_PACKAGE + ".domain";
  private static final String MODULE_USECASE = BASE_PACKAGE + ".usecase";
  private static final String MODULE_PERSISTENCE = BASE_PACKAGE + ".adapter.secondary";
  private static final JavaClasses ALL_CLASSES =
      new ClassFileImporter().importPackages(BASE_PACKAGE);
  private static final JavaClasses PROD_CLASSES =
      new ClassFileImporter().withImportOption(new DoNotIncludeTests())
          .importPackages(BASE_PACKAGE);

  @Test
  public void allClassesShouldNotUseJunitFourTest() {
    methods().that().haveNameContaining("Test")
        .should().beAnnotatedWith("org.junit.jupiter.api.Test")
        .check(ALL_CLASSES);
  }

  @Test
  public void methodsInTestClassesShouldBeNamedTest() {
    methods().that()
        .areAnnotatedWith("org.junit.jupiter.api.Test")

        .should().haveNameEndingWith("Test")
        .check(ALL_CLASSES);
  }

  @Test
  public void primaryShouldOnlyAccessedByUsecaseTest() {
    layeredArchitecture().consideringAllDependencies()
        .layer("Usecase").definedBy(MODULE_USECASE + "..")
        .layer("Primary Adapters").definedBy(MODULE_CONTROLLER + "..")
        .whereLayer("Primary Adapters").mayOnlyBeAccessedByLayers("Usecase")
        .check(ALL_CLASSES);
  }

  @Test
  public void secondaryShouldOnlyBeAccessedByUsecaseTest() {
    layeredArchitecture().consideringAllDependencies()
        .layer("Secondary Adapters").definedBy(MODULE_PERSISTENCE + "..")
        .layer("Usecase").definedBy(MODULE_USECASE + "..")
        .whereLayer("Secondary Adapters").mayOnlyBeAccessedByLayers("Usecase")
        .check(ALL_CLASSES);
  }

  @Test
  public void domainShouldOnlyBeAccessedByUsecaseTest() {
    layeredArchitecture().consideringAllDependencies()
        .layer("Domain").definedBy(MODULE_DOMAIN + "..")
        .layer("Usecase").definedBy(MODULE_USECASE + "..")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Usecase")
        .check(ALL_CLASSES);
  }

  @Test
  public void usecaseShouldOnlyBeAccessedByEverythingTest() {
    layeredArchitecture().consideringAllDependencies()
        .layer("Usecase").definedBy(MODULE_USECASE + "..")
        .layer("Domain").definedBy(MODULE_DOMAIN + "..")
        .layer("Primary Adapters").definedBy(MODULE_CONTROLLER + "..")
        .layer("Secondary Adapters").definedBy(MODULE_PERSISTENCE + "..")
        .whereLayer("Usecase")
        .mayOnlyBeAccessedByLayers("Domain", "Primary Adapters", "Secondary Adapters")
        .check(ALL_CLASSES);

  }


  @Test
  public void classesInControllerPackageShouldBeNamedRestControllerTest() {
    classes().that().resideInAPackage(MODULE_CONTROLLER)
        .should().haveSimpleNameEndingWith("RestController")
        .check(PROD_CLASSES);
  }

  @Test
  public void classesAnnotatedWithRepositoryShouldBeInPersistenceTest() {
    classes().that().areAssignableTo("org.springframework.data.repository.CrudRepository")
        .should().resideInAPackage(MODULE_PERSISTENCE)
        .check(PROD_CLASSES);
  }

  @Test
  public void classesInPersistencePackageShouldBeNamedRepositoryTest() {
    classes().that().resideInAPackage(MODULE_PERSISTENCE)
        .should().haveSimpleNameEndingWith("Repository")
        .check(PROD_CLASSES);
  }
}
