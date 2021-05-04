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
VERSION=latest DB_USERNAME=cpstemporal DB_PASSWORD=cpstemporal docker-compose up -d
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