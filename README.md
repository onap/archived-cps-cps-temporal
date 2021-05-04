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
mvn clean install -Pcps-temporal-docker -Dnexus.repository=
```

## Running Docker container

`docker-compose.yml` file is provided to be run with `docker-compose` tool and local image previously built.
It starts both Postgres Timescale database and CPS Temporal service.

Execute following command from project root folder:
```bash
VERSION=latest DB_USERNAME=cpstemporal DB_PASSWORD=cpstemporal docker-compose up -d
```
