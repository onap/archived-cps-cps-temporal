.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

=======================
CPS Temporal Deployment
=======================

* Deployment_
* Configuration_
* `Running on Local`_

Deployment
==========

CPS Temporal service is deployed using `ONAP OOM <https://docs.onap.org/projects/onap-oom/en/latest/index.html>`_
with Kubernetes Helm charts available in `OOM repository <https://gerrit.onap.org/r/gitweb?p=oom.git;a=tree;f=kubernetes/cps>`_.

Refer to `OOM User Guide <https://docs.onap.org/projects/onap-oom/en/latest/oom_user_guide.html>`_ for more information
about ONAP charts deployment.

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

Application and database credentials are kept in Kubernetes secrets. They are
defined as external secrets in
`values.yaml <https://github.com/onap/oom/blob/master/kubernetes/cps/components/cps-temporal/values.yaml#L28>`_
file.

Then, credential values from these secrets are configured in running container
as environment variables. This is done in
`deployment.yaml <https://github.com/onap/oom/blob/master/kubernetes/cps/components/cps-temporal/templates/deployment.yaml#L47>`_
file.

If no specific passwords are provided to the chart as override values for
deployment, then passwords are automatically generated when deploying and
creating Helm release. Once the deployment is completed, commands below can be
used to retrieve all credentials.

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

Running on Local
================

For development purposes, CPS Temporal can be ran on locally with Docker.
Refer to `README.md <https://github.com/onap/cps-cps-temporal/blob/master/README.md>`_
and `docker-compose.yml <https://github.com/onap/cps-cps-temporal/blob/master/docker-compose.yml>`_
files for more details.
