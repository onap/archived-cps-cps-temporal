.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

.. _release_notes:

==========================
CPS Temporal Release Notes
==========================

.. contents::
    :depth: 2
..

..      ========================
..      * * *   JAKARTA   * * *
..      ========================

Version: 1.1.0
==============

.. table::

   ===============================  ===============================
   **Docker Image**                 onap/cps-temporal:1.1.0
   **Designation**                  1.1.0 Jakarta
   **Date**                         2022 March 15
   ===============================  ===============================

Features
--------

* `CPS-747 <https://jira.onap.org/browse/CPS-747>`_ - Add support for Data Deletion Events
* `CPS-482 <https://jira.onap.org/browse/CPS-482>`_ - Add CSIT tests

Bug Fixes
---------

* `CPS-820 <https://jira.onap.org/browse/CPS-820>`_ - Upgrade log4j to 2.16.0
* `CPS-841 <https://jira.onap.org/browse/CPS-841>`_ - Upgrade log4j to 2.17.1 as recommended by ONAP SECCOM
* `CPS-905 <https://jira.onap.org/browse/CPS-905>`_ - Fix Docker images for CSIT tests

..      ========================
..      * * *   ISTANBUL   * * *
..      ========================

Version: 1.0.1
==============

.. table::

   ===============================  ===============================
   **Docker Image**                 onap/cps-temporal:1.0.1
   **Designation**                  1.0.1 Istanbul
   **Date**                         2021 January 6
   ===============================  ===============================

Features
--------
* None

Bug Fixes
---------

* `CPS-841 <https://jira.onap.org/browse/CPS-841>`_ - Update log4j version to 2.17.1 due to security vulnerability

Version: 1.0.0
==============

.. table::

   ===============================  ===============================
   **Docker Image**                 onap/cps-temporal:1.0.0
   **Designation**                  1.0.0 Istanbul
   **Date**                         2021 September 14
   ===============================  ===============================

New features
------------

* `CPS-369 <https://jira.onap.org/browse/CPS-369>`_ - Feature to store temporal data corresponding to CPS Core data node updates.
* `CPS-370 <https://jira.onap.org/browse/CPS-370>`_ - Feature to query temporal data from REST API.

Known Limitations, Issues and Workarounds
-----------------------------------------

* None

Security Notes
--------------

Known Security Issues:

* `CPS-488 <https://jira.onap.org/browse/CPS-488>`_ - Authentication and authorization for REST API is limited to one generic user.

Fixed Security Issues:

* None
