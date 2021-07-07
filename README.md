<!--
  ============LICENSE_START=======================================================
   Copyright (C) 2021 Bell Canada.
  ================================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  SPDX-License-Identifier: Apache-2.0
  ============LICENSE_END=========================================================
-->

# Building and running CPS Temporal locally

## Building Java Archive only

Following command builds Java executable jar to `target/cps-temporal-x.y.z-SNAPSHOT` JAR file
without generating any docker images:

```bash
mvn clean install
```

## Building Java Archive and local Docker image

Following command builds the JAR file and also generates the Docker image:

```bash
mvn clean install -Pcps-temporal-docker -Ddocker.repository.push=
```

## Running via Docker Compose

`docker-compose.yml` file is provided to be run with `docker-compose` tool and local image previously built.
It starts both Postgres Timescale database and CPS Temporal service.

Execute following command from project root folder:

```bash
docker-compose up -d
```

## Alternative local db setup

A Postgres instance with Timescale extension can be started by running the following command:

```
docker run --name postgres-cps-temporal -p 5433:5432 -d \
  -e POSTGRES_DB=cpstemporaldb \
  -e POSTGRES_USER=cpstemporal \
  -e POSTGRES_PASSWORD=cpstemporal \
  timescale/timescaledb:2.1.1-pg13
```

[Liquibase](https://www.liquibase.org/) is used to manage database schema changes and versions.
Then, the database schema is updated when the application is started or by running the following command:

```
mvn org.liquibase:liquibase-maven-plugin:4.3.2:update \
  -Dliquibase.url=jdbc:postgresql://localhost:5433/cpstemporaldb \
  -Dliquibase.username=cpstemporal \
  -Dliquibase.password=cpstemporal \
  -Dliquibase.changeLogFile=db/changelog/changelog-master.xml
```
