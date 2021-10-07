.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

===================
CPS Temporal Design
===================

Exposed APIs
============

CPS Temporal is providing a REST HTTP API to query historical CPS data.
Its OPEN API Specification can be found either:

* In :download:`openapi.yml <https://raw.githubusercontent.com/onap/cps-cps-temporal/master/openapi/swagger/openapi.yml>`
  file
* At ``https://<cps-temporal-host>:<cps-temporal-port>/swagger/openapi.yml``
  endpoint available on CPS Temporal running instance

Swagger UI is also available at:

* ``https://<cps-temporal-host>:<cps-temporal-port>/swagger-ui.html``

And following Postman collection can be used to send requests to any running
instance:

* :download:`CPS Temporal Postman Collection <../_static/postman-collections/cps-temporal-postman-collection.json>`

Event Integration
=================

CPS Core and CPS Temporal are integrated with an event driven architecture.
Integration between these two components is event notification based.

For each data modification handled by CPS Core,

* CPS Core is publishing, to a dedicated Kafka topic, an event representing
  the data configuration or state.
* CPS Temporal is listening to the same topic for the event and is responsible
  to keep track of all data over time.

Refer to :doc:`modeling` for more details on the event structure.
