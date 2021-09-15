.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

=======================
CPS Temporal Deployment
=======================

* Configuration_
* Logging_
* Monitoring_

CPS Temporal service is deployed using `ONAP OOM <https://docs.onap.org/projects/onap-oom/en/latest/index.html>`_
with Kubernetes Helm charts available in `OOM repository <https://gerrit.onap.org/r/gitweb?p=oom.git;a=tree;f=kubernetes/cps>`_.

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
|                                       |                                                                                                         |                               |
|                                       | This is the password to be used by CPS Temporal REST clients to authenticate themselves.                |                               |
|                                       |                                                                                                         |                               |
|                                       | If not defined, the password is generated when deploying the application.                               |                               |
|                                       |                                                                                                         |                               |
|                                       | See also :ref:`credentials_retrieval`.                                                                  |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| timescaledb.config.pgUserName         | Internal user name used bt CPS Temporal to connect to its own database.                                 | ``cpstemporal``               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| timescaledb.config.pgUserPassword     | Internal password used bt CPS Temporal to connect to its own database.                                  | Not defined                   |
|                                       |                                                                                                         |                               |
|                                       | If not defined, the password is generated when deploying the application.                               |                               |
|                                       |                                                                                                         |                               |
|                                       | See also :ref:`credentials_retrieval`.                                                                  |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Maximum number of elements that can be retrieved by a single REST API query request                     | ``20``                        |
| app.query.response.max-page-size      | using pagination feature.                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Maximum number of database connections in the connection pool.                                          | ``10``                        |
| spring.datasource.hikari.             |                                                                                                         |                               |
| maximumPoolSize                       |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka hostname and port                                                                                 | ``message-router-kafka:9092`` |
| spring.kafka.bootstrap-servers        |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka consumer group id                                                                                 | ``cps-temporal-group``        |
| spring.kafka.consumer.group-id        |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka topic to listen to                                                                                | ``cps.data-updated-events``   |
| app.listener.data-updated.topic       |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security protocol.                                                                                | ``PLAINTEXT``                 |
| spring.kafka.security.protocol        | Some possible values are:                                                                               |                               |
|                                       |                                                                                                         |                               |
|                                       | * ``PLAINTEXT``                                                                                         |                               |
|                                       | * ``SASL_PLAINTEXT``, for authentication                                                                |                               |
|                                       | * ``SASL_SSL``, for authentication and encryption                                                       |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL mechanism. Required for SASL_PLAINTEXT and SASL_SSL protocols.                      | Not defined                   |
| spring.kafka.properties.              | Some possible values are:                                                                               |                               |
| sasl.mechanism                        |                                                                                                         |                               |
|                                       | * ``PLAIN``, for PLAINTEXT                                                                              |                               |
|                                       | * ``SCRAM-SHA-512``, for SSL                                                                            |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL JAAS configuration. Required for SASL_PLAINTEXT and SASL_SSL protocols.             | Not defined                   |
| spring.kafka.properties.              | Some possible values are:                                                                               |                               |
| sasl.jaas.config                      |                                                                                                         |                               |
|                                       | * ``org.apache.kafka.common.security.plain.PlainLoginModule required username="..." password="...";``,  |                               |
|                                       |   for PLAINTEXT                                                                                         |                               |
|                                       | * ``org.apache.kafka.common.security.scram.ScramLoginModule required username="..." password="...";``,  |                               |
|                                       |   for SSL                                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL SSL store type. Required for SASL_SSL protocol.                                     | Not defined                   |
| spring.kafka.ssl.trust-store-type     | Some possible values are:                                                                               |                               |
|                                       |                                                                                                         |                               |
|                                       | * ``JKS``                                                                                               |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL SSL store file location. Required for SASL_SSL protocol.                            | Not defined                   |
| spring.kafka.ssl.trust-store-location |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL SSL store password. Required for SASL_SSL protocol.                                 | Not defined                   |
| spring.kafka.ssl.trust-store-password |                                                                                                         |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.additional.                    | Kafka security SASL SSL broker hostname identification verification. Required for SASL_SSL protocol.    | Not defined                   |
| spring.kafka.properties.              | Possible value is:                                                                                      |                               |
| ssl.endpoint.identification.algorithm |                                                                                                         |                               |
|                                       | * ``""``, empty string to disable                                                                       |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+

.. _credentials_retrieval:

Credentials Retrieval
---------------------

Commands below can be used to retrieve application property credentials.

For CPS Temporal REST API authentication:

.. code::

    # User
    kubectl get secrets <my-helm-release>-cps-temporal-app-user-creds -o yaml | grep login | awk '{print $2}' | base64 -D

    # Password
    kubectl get secrets <my-helm-release>-cps-temporal-app-user-creds -o yaml | grep password | awk '{print $2}' | base64 -D

For CSP Temporal database authentication:

.. code::

    # User
    kubectl get secrets <my-helm-release>-cps-temporal-pg-user-creds -o yaml | grep login | awk '{print $2}' | base64 -D

    # Password
    kubectl get secrets <my-helm-release>-cps-temporal-pg-user-creds -o yaml | grep password | awk '{print $2}' | base64 -D

Logging
=======

CPS Temporal logs are all sent to `stdout` in order to leverage Kubernetes
logging architecture. Then, these logs are available with following command:

.. code::

    kubectl logs <cps-temporal-pod>

This architecture, also makes all logs ready to be sent to an ELK or any
similar stack (not part of ONAP).

By default, root and cps loggers are set to `INFO` level.

Enabling tracing for all executed sql statements is done by changing hibernate
loggers log level.

Default logger configuration is provided as a chart resource: `logback.xml <hhttps://github.com/onap/oom/tree/master/kubernetes/cps>`_.

Monitoring
==========

Once CPS Temporal is deployed, from inside the Kubernetes cluster,
some information are available related to the running instance.

* Build Information and version

Build related information and version are exposed by the information endpoint.

.. code::

    http://cps-temporal:8081/manage/info

* Health

CPS Temporal status and state can be checked using health endpoint. This
endpoint encompasses readiness and liveness endpoints that are used for
Kubernetes probes.

.. code::

    http://cps-temporal:8081/manage/health

* Metrics

Prometheus metrics endpoint is available for more fine grain monitoring of CPS
Temporal component.

.. code::

    http://cps-temporal:8081/manage/prometheus
