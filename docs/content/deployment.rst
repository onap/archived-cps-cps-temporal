.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021-2022 Bell Canada
.. Modifications Copyright (C) 2021 Nordix Foundation

=======================
CPS Temporal Deployment
=======================

* Deployment_
* Configuration_
* `Running With Docker`_
* `Upgrade from Istanbul to Jakarta`_

Deployment
==========

Refer to :ref:`CPS-Deployment<onap-cps:deployment>`
page for deployment documentation related to CPS Temporal and all CPS components.

Once CPS Temporal is successfully deployed and running 2 pods are started,
one for running the service and another one for running the database instance:

.. code:: text

    NAME                                            READY   STATUS             RESTARTS   AGE
    cps-temporal-d4cf495b9-bbn7b                    1/1     Running            0          8h
    cps-temporal-db-0                               1/1     Running            0          8h

Configuration
=============

Application Properties
----------------------

The following table lists some properties that can be specified as Helm chart
values to configure the application to be deployed. This list is not
exhaustive.

+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| Property                              | Description                                                                                             | Default Value                 |
+=======================================+=========================================================================================================+===============================+
| config.appUserName                    | User name used by CPS Temporal service to configure the authentication for REST API it exposes.         | ``cpstemporaluser``           |
|                                       |                                                                                                         |                               |
|                                       | This is the user name to be used by CPS Temporal REST clients to authenticate themselves.               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.appUserPassword                | Password used by CPS Temporal service to configure the authentication for REST API it exposes.          | Not defined                   |
|                                       | This is the password to be used by CPS Temporal REST clients to authenticate themselves.                |                               |
|                                       | If not defined, the password is generated when deploying the application.                               |                               |
|                                       | See also :ref:`CPS Credentials Retrieval<onap-cps:cps_common_credentials_retrieval>`                    |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| timescaledb.config.pgUserName         | Internal user name used bt CPS Temporal to connect to its own database.                                 | ``cpstemporal``               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| timescaledb.config.pgUserPassword     | Internal password used bt CPS Temporal to connect to its own database.                                  | Not defined                   |
|                                       |                                                                                                         |                               |
|                                       | If not defined, the password is generated when deploying the application.                               |                               |
|                                       |                                                                                                         |                               |
|                                       | See also :ref:`credentials` section.                                                                    |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka hostname and port                                                                                 | ``message-router-kafka:9092`` |
| spring.kafka.bootstrap-servers        |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka consumer group id                                                                                 | ``cps-temporal-group``        |
| spring.kafka.consumer.group-id        |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka topic to listen to                                                                                | ``cps.data-updated-events``   |
| app.listener.data-updated.topic       |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security protocol.                                                                                | ``PLAINTEXT``                 |
| spring.kafka.security.protocol        | Some possible values are:                                                                               |                               |
|                                       |                                                                                                         |                               |
|                                       | * ``PLAINTEXT``                                                                                         |                               |
|                                       | * ``SASL_PLAINTEXT``, for authentication                                                                |                               |
|                                       | * ``SASL_SSL``, for authentication and encryption                                                       |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL mechanism. Required for SASL_PLAINTEXT and SASL_SSL protocols.                      | Not defined                   |
| spring.kafka.properties.              | Some possible values are:                                                                               |                               |
| sasl.mechanism                        |                                                                                                         |                               |
|                                       | * ``PLAIN``, for PLAINTEXT                                                                              |                               |
|                                       | * ``SCRAM-SHA-512``, for SSL                                                                            |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL JAAS configuration. Required for SASL_PLAINTEXT and SASL_SSL protocols.             | Not defined                   |
| spring.kafka.properties.              | Some possible values are:                                                                               |                               |
| sasl.jaas.config                      |                                                                                                         |                               |
|                                       | * ``org.apache.kafka.common.security.plain.PlainLoginModule required username="..." password="...";``,  |                               |
|                                       |   for PLAINTEXT                                                                                         |                               |
|                                       | * ``org.apache.kafka.common.security.scram.ScramLoginModule required username="..." password="...";``,  |                               |
|                                       |   for SSL                                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL SSL store type. Required for SASL_SSL protocol.                                     | Not defined                   |
| spring.kafka.ssl.trust-store-type     | Some possible values are:                                                                               |                               |
|                                       |                                                                                                         |                               |
|                                       | * ``JKS``                                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL SSL store file location. Required for SASL_SSL protocol.                            | Not defined                   |
| spring.kafka.ssl.trust-store-location |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL SSL store password. Required for SASL_SSL protocol.                                 | Not defined                   |
| spring.kafka.ssl.trust-store-password |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.eventConsumption.              | Kafka security SASL SSL broker hostname identification verification. Required for SASL_SSL protocol.    | Not defined                   |
| spring.kafka.properties.              | Possible value is:                                                                                      |                               |
| ssl.endpoint.identification.algorithm |                                                                                                         |                               |
|                                       | * ``""``, empty string to disable                                                                       |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Maximum number of elements that can be retrieved by a single REST API query request                     | ``20``                        |
| app.query.response.max-page-size      | using pagination feature.                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Maximum number of database connections in the connection pool.                                          | ``10``                        |
| spring.datasource.hikari.             |                                                                                                         |                               |
| maximumPoolSize                       |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+

.. _credentials:

Credentials
-----------

Once the deployment is completed, refer to :ref:`CPS Credentials Retrieval<onap-cps:cps_common_credentials_retrieval>`
 for more information related to credentials retrieval.

Running With Docker
===================

For development purposes, CPS Temporal can be ran on any environment using
Docker. Refer to `README.md <https://github.com/onap/cps-cps-temporal/blob/jakarta/README.md>`_
and `docker-compose.yml <https://github.com/onap/cps-cps-temporal/blob/jakarta/docker-compose.yml>`_
files for more details.

Upgrade from Istanbul to Jakarta
================================

Cps data-updated-event schema v2
--------------------------------

Cps data-updated-event schema v2 used in Jakarta is backward compatible with data-updated-event schema v1 used in Istanbul.

It means that consumers using the schema v2 can process events generated by producers using the schema v2 or v1.

This implies that cps temporal (consumer) must be upgraded from Istanbul to Jakarta at the same time or before cps core (producer) is upgraded from Istanbul to Jakarta.

Database
--------

In Jakarta, CPS Temporal database is upgraded from TimescaleDB ``2.1.1`` running PostgresSQL ``13.2`` to
TimescaleDB ``2.5.1`` running PosgresSQL ``14.1``. This is a major PostgresSQL upgrade subject to change data storage
format. Then, any existing CPS Temporal data from Istanbul needs to be migrated before it can be used in Jakarta.

The migration needs to be done in 2 main sequential steps for both TimescaleDB and PostgresSQL:

#. Upgrade TimescaleDB from 2.1.1 to 2.5.1
#. Upgrade PostgresSQL form 13.2 to 14.1

Bellow are the detailed steps to be completed for data migration (manual docker steps, to be reviewed for
production upgrade).

* Stop CPS Temporal Istanbul service.

* Backup 2.1.1-pg13 data. This backup is to be used to restore data if needed.

.. code:: text

    # Start timescale 2.1.1-pg13 db instance
    docker run --name postgres-cps-temporal-2.1.1-13 -d \
      -e POSTGRES_DB=cpstemporaldb \
      -e POSTGRES_USER=cpstemporal \
      -e POSTGRES_PASSWORD=cpstemporal \
      -v cps-temporal_data:/var/lib/postgresql/data \
      -v cps-temporal_backup-2.1.1-13:/var/lib/postgresql/backup \
      timescale/timescaledb:2.1.1-pg13

    docker exec -it postgres-cps-temporal-2.1.1-13 psql -d cpstemporaldb -U cpstemporal -c "select version();"
    docker exec -it postgres-cps-temporal-2.1.1-13 psql -d cpstemporaldb -U cpstemporal -c "\dx timescaledb"

    # Backup 2.1.1-pg13 data
    docker exec -it postgres-cps-temporal-2.1.1-13 pg_basebackup -U cpstemporal -D /var/lib/postgresql/backup/
    docker exec -it postgres-cps-temporal-2.1.1-13 ls -l /var/lib/postgresql/backup/

    # Stop db instance
    docker container stop postgres-cps-temporal-2.1.1-13
    docker container rm postgres-cps-temporal-2.1.1-13

* Upgrade data from 2.1.1-pg13 to 2.5.1-pg-13

.. code:: text

    # Start timescale 2.5.1.-pg13
    docker run --name postgres-cps-temporal-2.5.1-13 -d \
      -e POSTGRES_DB=cpstemporaldb \
      -e POSTGRES_USER=cpstemporal \
      -e POSTGRES_PASSWORD=cpstemporal \
      -v cps-temporal_data:/var/lib/postgresql/data \
      -v cps-temporal_dump-2.5.1-13:/var/lib/postgresql/dump \
      timescale/timescaledb:2.5.1-pg13

    # Upgrade data to 2.5.1-pg13
    docker exec -it postgres-cps-temporal-2.5.1-13 psql -d cpstemporaldb -U cpstemporal -c "\dx timescaledb"
    docker exec -it postgres-cps-temporal-2.5.1-13 psql -X -d cpstemporaldb -U cpstemporal -c "ALTER EXTENSION timescaledb UPDATE;"
    docker exec -it postgres-cps-temporal-2.5.1-13 psql -d cpstemporaldb -U cpstemporal -c "\dx timescaledb"

* Create a dump of 2.5.1-pg-13 data

.. code:: text

    # Dump 2.5.1-pg13 data
    docker exec -it postgres-cps-temporal-2.5.1-13 pg_dump -d cpstemporaldb -U cpstemporal -Fc -f /var/lib/postgresql/dump/dumpfile
    docker exec -it postgres-cps-temporal-2.5.1-13 ls -l /var/lib/postgresql/dump/dumpfile

    # Stop db instance
    docker container stop postgres-cps-temporal-2.5.1-13
    docker container rm postgres-cps-temporal-2.5.1-13

* Upgrade data from 2.5.1-pg13 to 2.5.1-pg-14, by importing the dump in PostgresSQL 14 instance

.. code:: text

    # Start timescale 2.5.1-pg14
    docker run --name postgres-cps-temporal-2.5.1-14 -p 5432:5432 -d \
      -e POSTGRES_DB=cpstemporaldb \
      -e POSTGRES_USER=cpstemporal \
      -e POSTGRES_PASSWORD=cpstemporal \
      -v cps-temporal_data-2.5.1-14:/var/lib/postgresql/data \
      -v cps-temporal_dump-2.5.1-13:/var/lib/postgresql/dump \
      timescale/timescaledb:2.5.1-pg14

    docker exec -it postgres-cps-temporal-2.5.1-14 psql -d cpstemporaldb -U cpstemporal -c "select version();"
    docker exec -it postgres-cps-temporal-2.5.1-14 psql -d cpstemporaldb -U cpstemporal -c "\dx timescaledb"

    # Upgrade data to 2.5.1-pg14 by restoring the dump
    docker exec -it postgres-cps-temporal-2.5.1-14 pg_restore -d cpstemporaldb -U cpstemporal -Fc /var/lib/postgresql/dump/dumpfile
    docker exec -it postgres-cps-temporal-2.5.1-14 psql -d cpstemporaldb -U cpstemporal -c "select count(*) from network_data;"

    # Stop db instances
    docker container stop postgres-cps-temporal-2.5.1-14
    docker container rm postgres-cps-temporal-2.5.1-14

* Copy 2.5.1-pg14 data to data volume

.. code:: text

    # Start busybox
    docker run -it --rm \
      -v cps-temporal_data:/data \
      -v cps-temporal_data-2.5.1-14:/data-2.5.1-14 \
      busybox:1.34.1

    # Run commands in busybox
    rm -rf /data/*
    cp -rp /data-2.5.1-14/* /data
    diff -r /data /data-2.5.1-14
    exit

* Start Cps Temporal Jakarta service

* Cleanup volumes that are not needed anymore

For more details about TimescaleDB and PostgresSQL upgrades, refer to:

* `Updating TimescaleDB versions <https://docs.timescale.com/timescaledb/latest/how-to-guides/update-timescaledb/>`_
* `Upgrading a PostgreSQL Cluster <https://www.postgresql.org/docs/14/upgrading.html>`_
