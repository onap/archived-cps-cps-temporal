/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.repository

import org.onap.cps.tempoal.repository.containers.TimescaleContainer
import org.onap.cps.temporal.domain.NetworkData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.time.OffsetDateTime

@SpringBootTest
@Testcontainers
class NetworkDataRepositorySpec extends Specification {

    def observedTimestamp = OffsetDateTime.now()
    def dataspaceName = 'TEST_DATASPACE'
    def schemaSetName = 'TEST_SCHEMA_SET'
    def anchorName = 'TEST_ANCHOR'
    def payload = '{ \"message\": \"Hello World!\" }'

    @Autowired
    NetworkDataRepository networkDataRepository

    def networkData = NetworkData.builder().observedTimestamp(observedTimestamp).dataspace(dataspaceName)
            .schemaSet(schemaSetName).anchor(anchorName).payload(payload).build()

    @Shared
    def databaseTestContainer = TimescaleContainer.getInstance()

    def setupSpec() {
        databaseTestContainer.start()
    }

    def 'Store latest network data in timeseries database.'() {
        when: 'a new Network Data is stored'
            NetworkData storedData = networkDataRepository.save(networkData)
        then: ' the saved Network Data is returned'
            storedData.getDataspace() == networkData.getDataspace()
            storedData.getSchemaSet() == networkData.getSchemaSet()
            storedData.getAnchor() == networkData.getAnchor()
            storedData.getPayload() == networkData.getPayload()
            storedData.getObservedTimestamp() == networkData.getObservedTimestamp()
        and: ' createdTimestamp is auto populated by jpa'
            storedData.getCreatedTimestamp() != null
        and: ' the CreationTimestamp is ahead of ObservedTimestamp'
            storedData.getCreatedTimestamp() > networkData.getObservedTimestamp()
    }
}
