.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021-2022 Bell Canada

=====================
CPS Temporal Modeling
=====================

Event Structure
===============

Data manipulated by both CPS Core and CPS Temporal to represent a Data Updated
Event is a JSON structure that is defined by following Json Schema:

* :download:`cps-data-updated-event-schema.json <../_static/event-schema/cps-data-updated-event-schema-v1.json>`

And following is an example of an event compliant with this schema:

.. code:: json

    {
        "schema": "urn:cps:org.onap.cps:data-updated-event-schema:v1",
        "id": "38aa6cc6-264d-4ede-b534-18f5c1f403ea",
        "source": "urn:cps:org.onap.cps",
        "type": "org.onap.cps.data-updated-event",
        "content": {
            "observedTimestamp": "2021-06-09T13:00:00.123-0400",
            "operation": "UPDATE",
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

