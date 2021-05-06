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

package org.onap.cps.tempoal.repository.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Container for timescale database.
 */
public class TimescaleContainer extends PostgreSQLContainer<TimescaleContainer> {

    private static final String IMAGE_NAME = "timescale/timescaledb:2.1.1-pg13";
    private static final DockerImageName DOCKER_IMAGE_NAME =
            DockerImageName.parse(IMAGE_NAME).asCompatibleSubstituteFor("postgres");

    private static TimescaleContainer container;

    private TimescaleContainer() {
        super(DOCKER_IMAGE_NAME);
    }

    /**
     * Get the unique container instance.
     * @return the container instance.
     */
    public static TimescaleContainer getInstance() {
        if (container == null) {
            container = new TimescaleContainer();
            Runtime.getRuntime().addShutdownHook(new Thread(container::terminate));
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // Do nothing on test completion, container removal is performed via terminate() on JVM shutdown.
    }

    private void terminate() {
        super.stop();
    }

}
