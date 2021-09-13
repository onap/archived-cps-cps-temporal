# ============LICENSE_START=======================================================
# Copyright (c) 2021 Pantheon.tech.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================

*** Settings ***
Documentation         CPS Core - REST API

Library               Collections
Library               OperatingSystem
Library               RequestsLibrary

Suite Setup           Create Session      CPS_URL    http://${CPS_HOST}:${CPS_PORT}

*** Variables ***

${auth}                 Basic Y3BzdXNlcjpjcHNyMGNrcyE=
${basePath}             /cps/api
${dataspaceName}        CSIT-Dataspace
${schemaSetName}        CSIT-SchemaSet
${anchorName}           CSIT-Anchor

*** Test Cases ***
Create Dataspace
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces
    ${params}=          Create Dictionary   dataspace-name=${dataspaceName}
    ${headers}=         Create Dictionary   Authorization=${auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Schema Set from YANG file
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces/${dataspaceName}/schema-sets
    ${params}=          Create Dictionary   schema-set-name=${schemaSetName}
    ${fileData}=        Get Binary File     ${DATADIR}${/}test-tree.yang
    ${fileTuple}=       Create List         test.yang   ${fileData}   application/zip
    &{files}=           Create Dictionary   file=${fileTuple}
    ${headers}=         Create Dictionary   Authorization=${auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   files=${files}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Anchor
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces/${dataspaceName}/anchors
    ${params}=          Create Dictionary   schema-set-name=${schemaSetName}   anchor-name=${anchorName}
    ${headers}=         Create Dictionary   Authorization=${auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Data Node
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/nodes
    ${headers}          Create Dictionary   Content-Type=application/json   Authorization=${auth}
    ${jsonData}=        Get Binary File     ${DATADIR}${/}test-tree.json
    ${response}=        POST On Session     CPS_URL   ${uri}   headers=${headers}   data=${jsonData}
    Should Be Equal As Strings              ${response.status_code}   201

Update Data Node
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/nodes
    ${headers}          Create Dictionary   Content-Type=application/json   Authorization=${auth}
    ${params}=          Create Dictionary   xpath=/test-tree/branch[@name='Left']
    ${jsonData}=        Get Binary File     ${DATADIR}${/}update-test-tree.json
    ${response}=        PATCH On Session     CPS_URL   ${uri}   params=${params}  headers=${headers}   data=${jsonData}
    Should Be Equal As Strings              ${response.status_code}   200
