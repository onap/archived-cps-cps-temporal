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
Documentation         CPS Temporal REST API

Library               Collections
Library               OperatingSystem
Library               RequestsLibrary

Suite Setup           Create Session      CPS_URL    http://${CPS_HOST}:${CPS_PORT}

*** Variables ***

${cps_auth}                          Basic Y3BzdXNlcjpjcHNyMGNrcyE=
${cps_basePath}                      /cps/api
${cps_temporal_auth}                 Basic Y3BzdGVtcG9yYWw6Y3BzdGVtcG9yYWw=
${cps_temporal_basePath}             /cps-temporal/api
${dataspaceName}                     CSIT-Dataspace
${schemaSetName}                     CSIT-SchemaSet
${anchorName}                        CSIT-Anchor
${observedTimestampCreation}         2021-03-21T00:00:00.000+0000
${observedTimestampUpdate}           2021-05-21T00:00:00.000+0000


*** Test Cases ***
Create Dataspace
    ${uri}=             Set Variable        ${cps_basePath}/v1/dataspaces
    ${params}=          Create Dictionary   dataspace-name=${dataspaceName}
    ${headers}=         Create Dictionary   Authorization=${cps_auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Schema Set from YANG file
    ${uri}=             Set Variable        ${cps_basePath}/v1/dataspaces/${dataspaceName}/schema-sets
    ${params}=          Create Dictionary   schema-set-name=${schemaSetName}
    ${fileData}=        Get Binary File     ${DATADIR}${/}test-tree.yang
    ${fileTuple}=       Create List         test.yang   ${fileData}   application/zip
    &{files}=           Create Dictionary   file=${fileTuple}
    ${headers}=         Create Dictionary   Authorization=${cps_auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   files=${files}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Anchor
    ${uri}=             Set Variable        ${cps_basePath}/v1/dataspaces/${dataspaceName}/anchors
    ${params}=          Create Dictionary   schema-set-name=${schemaSetName}   anchor-name=${anchorName}
    ${headers}=         Create Dictionary   Authorization=${cps_auth}
    ${response}=        POST On Session     CPS_URL   ${uri}   params=${params}   headers=${headers}
    Should Be Equal As Strings              ${response.status_code}   201

Create Data Node
    ${uri}=             Set Variable        ${cps_basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/nodes
    ${headers}          Create Dictionary   Content-Type=application/json  accept=text/plain  Authorization=${cps_auth}
    ${params}=          Create Dictionary   observed-timestamp=${observedTimestampCreation}
    ${jsonData}=        Get Binary File     ${DATADIR}${/}test-tree.json
    ${response}=        POST On Session     CPS_URL   ${uri}   params=${params}   headers=${headers}   data=${jsonData}
    Should Be Equal As Strings              ${response.status_code}   201

Get Anchor History by dataspace and anchor name when Data node is created
    Create Session      CPS_TEMPORAL_URL    http://${CPS_TEMPORAL_HOST}:${CPS_TEMPORAL_PORT}
    ${uri}=             Set Variable        ${cps_temporal_basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/history
    ${headers}=         Create Dictionary   Authorization=${cps_temporal_auth}
    ${response}=        Get On Session      CPS_TEMPORAL_URL   ${uri}  headers=${headers}   expected_status=200
    ${responseJson}=    Set Variable        ${response.json()}
    Should Be Equal As Strings              ${response.status_code}       200
    Length Should Be                        ${responseJson['records']}    1
    Should Be Equal As Strings              ${responseJson['records'][0]['observedTimestamp']}   ${observedTimestampCreation}
    Should Not Be Equal As Strings          ${responseJson['records'][0]['observedTimestamp']}   ${observedTimestampUpdate}

Update Data Node
    ${uri}=             Set Variable        ${cps_basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/nodes
    ${headers}          Create Dictionary   Content-Type=application/json  Authorization=${cps_auth}
    ${params}=          Create Dictionary   xpath=/test-tree/branch[@name='Left']   observed-timestamp=${observedTimestampUpdate}
    ${jsonData}=        Get Binary File     ${DATADIR}${/}update-test-tree.json
    ${response}=        PATCH On Session    CPS_URL   ${uri}   params=${params}  headers=${headers}   data=${jsonData}
    Should Be Equal As Strings              ${response.status_code}   200

Get Anchor History by dataspace and anchor name with updated data node
    Create Session      CPS_TEMPORAL_URL    http://${CPS_TEMPORAL_HOST}:${CPS_TEMPORAL_PORT}
    ${uri}=             Set Variable        ${cps_temporal_basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/history
    ${headers}=         Create Dictionary   Authorization=${cps_temporal_auth}
    ${params}=          Create Dictionary   sort=observed-timestamp:desc
    ${response}=        Get On Session      CPS_TEMPORAL_URL   ${uri}  headers=${headers}   expected_status=200
    ${responseJson}=    Set Variable        ${response.json()}
    Should Be Equal As Strings              ${response.status_code}       200
    Length Should Be                        ${responseJson['records']}    2
    Should Be Equal As Strings              ${responseJson['records'][1]['observedTimestamp']}   ${observedTimestampCreation}
    Should Be Equal As Strings              ${responseJson['records'][0]['observedTimestamp']}   ${observedTimestampUpdate}
