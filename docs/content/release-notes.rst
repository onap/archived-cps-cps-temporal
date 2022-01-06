.. This work is licensed under a
.. Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
..
.. Copyright (C) 2021 Bell Canada

==========================
CPS Temporal Release Notes
==========================

.. contents::
    :depth: 2
..

..      ========================
..      * * *   JAKARTA   * * *
..      ========================

Version: 1.1.0-SNAPSHOT
=======================

This section lists the main changes & fixes merged into master (snapshot) version of CPS-Temporal. This information is here to assist developers that want experiment/test using our latest code bases directly. Stability of this is not guaranteed.

Features
--------
* None

Bug Fixes
---------

   - `CPS-841 <https://jira.onap.org/browse/CPS-841>`_  Upgrade log4j to 2.17.1 as recommended by ONAP SECCOM

Version: 1.0.0
==============

* Release Date: 2021-09-14 (Istanbul)

Artifacts released
------------------

.. table::

   ===============================  ===============================
   **Repository**                   **Docker Image**
   onap/cps-temporal                onap/cps-temporal:1.0.0
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
