/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Test class responsible for layered architecture.
 */
@AnalyzeClasses(packages = "org.onap.cps.temporal", importOptions = { ImportOption.DoNotIncludeTests.class })
public class LayeredArchitectureTest {

    private static final String CONTROLLER_PACKAGE = "org.onap.cps.temporal.controller..";
    private static final String SERVICE_PACKAGE = "org.onap.cps.temporal.service..";
    private static final String REPOSITORY_PACKAGE = "org.onap.cps.temporal.repository..";

    // 'access' catches only violations by real accesses,
    // i.e. accessing a field, calling a method; compare 'dependOn' further down

    @ArchTest
    public static final ArchRule layeredArchitectureRule =
            layeredArchitecture()
                    .layer("Controller").definedBy(CONTROLLER_PACKAGE)
                    .layer("Service").definedBy(SERVICE_PACKAGE)
                    .layer("Repository").definedBy(REPOSITORY_PACKAGE)
                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    // 'dependOn' catches a wider variety of violations,
    // e.g. having fields of type, having method parameters of type, extending type ...

    @ArchTest
    static final ArchRule controllerDependencyRule =
            classes().that().resideInAPackage(CONTROLLER_PACKAGE)
                    .should().onlyHaveDependentClassesThat()
                    .resideInAPackage(CONTROLLER_PACKAGE);

    @ArchTest
    static final ArchRule serviceDependencyRule =
            classes().that().resideInAPackage(SERVICE_PACKAGE)
                    .should().onlyHaveDependentClassesThat()
                    .resideInAnyPackage(CONTROLLER_PACKAGE, SERVICE_PACKAGE);

    @ArchTest
    static final ArchRule repositoryDependencyRule =
            classes().that().resideInAPackage(REPOSITORY_PACKAGE)
                    .should().onlyHaveDependentClassesThat()
                    .resideInAnyPackage(SERVICE_PACKAGE, REPOSITORY_PACKAGE);

}