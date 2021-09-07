.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

=========================
CPS Temporal Architecture
=========================

CPS Temporal is a dedicated service, **distinct** and **decoupled** from CPS
Core. CPS Temporal service is an **independently deployable** unit.

Integration between Core and Temporal is **event notification based,
asynchronous, send and forget**. By doing this, we are avoiding the dependency
from CPS Core on CPS Temporal and its API. Actually, it reverses the
dependency, which makes more sense from a conceptual point of view.

For each data modification handled by CPS Core,

* CPS Core is **publishing**, to a dedicated topic, an event representing the
  data configuration or state.
* CPS Temporal is **listening** to the same topic for the event and is
  responsible to keep track of all data over time.

In the future, some other services can be created to listen to the same topic
in order to implement additional functionalities or storage forms.

The event messaging system for this integration is **Kafka**, whose running
instance is deployed independently from CPS. It could be either:

* the ONAP Kafka instance from ONAP DMaaP component,
* or any specific Kaka instance deployed independently from ONAP

The following diagram is the C4 Model representing CPS System Containers:

.. image:: /_static/images/cps-temporal-c4-container.png
   :alt: C4 Model Diagram: Containers for CPS Software system
