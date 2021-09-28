.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

========================
CPS Temporal Admin Guide
========================

* Configuration_
* Logging_
* Monitoring_

Configuration
=============

Refer Deployment Configuration for application properties available to configure the application.

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

Default logger configuration is provided as a chart resource: `logback.xml <https://github.com/onap/oom/blob/master/kubernetes/cps/components/cps-temporal/resources/config/logback.xml>`_.

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
