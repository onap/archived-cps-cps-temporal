# CPS Temporal Service

For now, this repo contains a very minimalist skeleton of the application.

## Local DB setup

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
