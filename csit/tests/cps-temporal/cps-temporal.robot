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

Suite Setup           Create Session      CPS_TEMPORAL_URL    http://${CPS_TEMPORAL_HOST}:${CPS_TEMPORAL_PORT}

*** Variables ***

${auth}                 Basic Y3BzdGVtcG9yYWw6Y3BzdGVtcG9yYWw=
${basePath}             /cps-temporal/api
${dataspaceName}        CSIT-Dataspace
${schemaSetName}        CSIT-SchemaSet
${anchorName}           CSIT-Anchor
${ranDataspaceName}     NFP-Operational
${ranSchemaSetName}     cps-ran-schema-model

*** Test Cases ***
Get Data Node by XPath
    ${uri}=             Set Variable        ${basePath}/v1/dataspaces/${dataspaceName}/anchors/${anchorName}/history
    ${headers}=         Create Dictionary   Authorization=${auth}
    ${response}=        Get On Session      CPS_TEMPORAL_URL   ${uri}  headers=${headers}   expected_status=200
    ${responseJson}=    Set Variable        ${response.json()}
    Should Be Equal As Strings              ${response.status_code}   200
    Length Should Be                        ${responseJson['records']}  2
