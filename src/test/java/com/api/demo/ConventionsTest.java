package com.api.demo;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import lombok.Generated;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

// Ensures architectural conventions are followed
class ConventionsTest {

    private static final JavaClasses SOURCES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.api.demo");

    private static final JavaClasses TESTS = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .importPackages("com.api.demo");

    @Test
    @ArchTest
    void noClassShouldDefineStaticLogger() {
        ArchRule rule = ArchRuleDefinition.fields()
                .that().haveRawType(Logger.class)
                .or().haveRawType(java.util.logging.Logger.class)
                .should().beAnnotatedWith(Generated.class)
                .because("Logging must be done through @Sl4j annotation that Lombok generates");
        rule.check(SOURCES);
    }
}
