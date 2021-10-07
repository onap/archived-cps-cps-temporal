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

* In `openapi.yml <https://github.com/onap/cps-cps-temporal/blob/master/openapi/swagger/openapi.yml>`_
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

Data manipulated by both CPS Core and CPS Temporal to represent a Data Updated
Event is a JSON structure that is defined by following Json Schema:

* `cps-data-updated-event-schema.json <https://github.com/onap/cps/blob/master/cps-events/src/main/resources/schemas/cps-data-updated-event-schema.json>`_

And following is an example of an event compliant with this schema:

.. code:: json

    {
        "schema": "urn:cps:org.onap.cps:data-updated-event-schema:v1",
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
