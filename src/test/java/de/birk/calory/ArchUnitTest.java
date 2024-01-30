package de.birk.calory;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class ArchUnitTest {

    private static final String BASE_PACKAGE = "de.birk.calory";
    private static final String MODULE_CONTROLLER = BASE_PACKAGE + ".controller";
    private static final String MODULE_DOMAIN = BASE_PACKAGE + ".domain";
    private static final String MODULE_SERVICE = BASE_PACKAGE + ".service";
    private static final String MODULE_PERSISTENCE = BASE_PACKAGE + ".persistence";
    private static final JavaClasses ALL_CLASSES = new ClassFileImporter().importPackages(BASE_PACKAGE);
    private static final JavaClasses TEST_CLASSES = new ClassFileImporter().

    @Test
    public void allClassesShouldNotUseJunitFourTest() {
        methods().that().haveNameContaining("Test")
                .should().beAnnotatedWith("org.junit.jupiter.api.Test")
                .check(ALL_CLASSES);
    }

    @Test
    public void methodsInTestClassesShouldBeNamedTest(){
        methods().that().areDeclaredInClassesThat().areAnnotatedWith("org.junit.jupiter.api.Test")
                .should().haveNameEndingWith("Test")
                .check();
    }

    //TODO Marius ArchUnitTest für zyklische Abhängigkeiten ist sinnvoll
    @Test
    public void cyclicDependenciesAreNotAllowedTest() {

    }

    @Test
    public void classesInControllerPackageShouldBeNamedRestControllerTest() {
        classes().that().resideInAPackage(MODULE_CONTROLLER)
                .should().haveSimpleNameEndingWith("RestController")
                .check(ALL_CLASSES);
    }

    @Test
    public void classesAnnotatedWithRepositoryShouldBeInPersistenceTest() {
        classes().that().areAssignableTo("org.springframework.data.repository.CrudRepository")
                .should().resideInAPackage(MODULE_PERSISTENCE)
                .check(ALL_CLASSES);
    }

    @Test
    public void classesInPersistencePackageShouldBeNamedRepositoryTest() {
        classes().that().resideInAPackage(MODULE_PERSISTENCE)
                .should().haveSimpleNameEndingWith("Repository")
                .check(ALL_CLASSES);
    }
}
