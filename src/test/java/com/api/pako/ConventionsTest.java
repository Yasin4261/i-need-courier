package com.api.pako;

import com.api.pako.dto.ApiResponse;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import lombok.Generated;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

// Ensures architectural conventions are followed
class ConventionsTest {

    private static final JavaClasses SOURCES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.api.pako");

    private static final JavaClasses TESTS = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .importPackages("com.api.pako");

    @Test
    @ArchTest
    void noClassShouldDefineStaticLogger() {
        var rule = ArchRuleDefinition.fields()
                .that().haveRawType(Logger.class)
                .or().haveRawType(java.util.logging.Logger.class)
                .should().beAnnotatedWith(Generated.class)
                .because("Logging must be done through @Sl4j annotation that Lombok generates");
        rule.check(SOURCES);
    }

    @Test
    @ArchTest
    void noAutowiredUseInSourceCodeInFavorOfConstructorInjection() {
        var rule = ArchRuleDefinition.noFields()
                .should().beAnnotatedWith(Autowired.class)
                .because("Prefer constructor injection over field injection via Autowired");
        rule.check(SOURCES);
    }

    @Test
    @ArchTest
    void controllersAlwaysReturnApiResponse() {
        var rule = ArchRuleDefinition.methods()
                .that().arePublic()
                .and().areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .should().haveRawReturnType(ApiResponse.class)
                .because("Controllers should always return ApiResponse for consistency and error handling");
        rule.check(SOURCES);
    }
}
