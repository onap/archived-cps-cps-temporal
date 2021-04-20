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

package org.onap.cps.temporal;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.onap.cps.temporal.repository.TimescaleContainer;
import org.springframework.boot.test.context.SpringBootTest;

// This test class without any assertion is obviously not really useful.
// Its only purpose is to be able to cover current code.
// It should be deleted when more code will be added to the project.
@SpringBootTest
class ApplicationTest {

    private static final TimescaleContainer TIMESCALE_CONTAINER = TimescaleContainer.getInstance();

    static {
        TIMESCALE_CONTAINER.start();
    }

    @Test
    void testMain() {
        Application.main(Arrays.array());
    }

}
