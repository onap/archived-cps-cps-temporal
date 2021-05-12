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

import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Test class responsible for dependencies validations.
 */
@AnalyzeClasses(packages = "org.onap.cps.temporal", importOptions = { ImportOption.DoNotIncludeTests.class })
public class DependencyArchitectureTest {

    @ArchTest
    static final ArchRule noCyclesRule =
            slices().matching("org.onap.cps.temporal.(**)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule noUpperPackageDependencyRule = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

}
