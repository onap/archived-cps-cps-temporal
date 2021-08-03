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
mvn clean install -Pdocker -Ddocker.repository.push=
```

## Running via Docker Compose

`docker-compose.yml` file is provided to be run with `docker-compose` tool and local image previously built.
It starts following services:

* CPS Temporal service (cps-temporal)
* Postgres Timescale database (timescaledb)
* Kafka broker (zookeeper and kafka)

Execute following command from project root folder to start all services:

```bash
docker-compose up
```

Then, use `kafkacat` tool to produce a data updated event into the Kafka topic:

```bash
docker run -i --rm --network=host edenhill/kafkacat:1.6.0 -b localhost:19092 -t cps.cfg-state-events -D/ -P <<EOF
{
    "schema": "urn:cps:org.onap.cps:data-updated-event-schema:v0",
    "id": "38aa6cc6-264d-4ede-b534-18f5c1f403ea",
    "source": "urn:cps:org.onap.cps",
    "type": "org.onap.cps.data-updated-event",
    "content": {
        "observedTimestamp": "2021-06-09T13:00:00.123-0400",
        "dataspaceName": "my-dataspace",
        "schemaSetName": "my-schema-set",
        "anchorName": "my-anchor",
        "data": {
            "interface": {
                "name": "itf-1",
                "status": "up"
            }
        }
    }
}
EOF
```

Finally, verify that CPS Temporal data is persisted as expected:

```bash
psql -h localhost -p 5433 -d cpstemporaldb -U cpstemporal -c \
  "select * from network_data order by created_timestamp desc limit 1"
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

## Accessing services

Swagger UI is available to discover service endpoints and send requests.

* `http://localhost:<port-number>/swagger-ui.html`

