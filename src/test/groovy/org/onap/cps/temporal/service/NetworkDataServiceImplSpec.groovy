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

package org.onap.cps.temporal.service

import java.time.OffsetDateTime
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import spock.lang.Specification

class NetworkDataServiceImplSpec extends Specification {

    def observedTimestamp = OffsetDateTime.now()
    def dataspaceName = 'TEST_DATASPACE'
    def schemaSetName = 'TEST_SCHEMA_SET'
    def anchorName = 'TEST_ANCHOR'
    def payload = '{ \"message\": \"Hello World!\" }'

    def objectUnderTest = new NetworkDataServiceImpl()

    def mockNetworkDataRepository = Mock(NetworkDataRepository)

    def networkData = NetworkData.builder().observedTimestamp(observedTimestamp).dataspace(dataspaceName)
            .schemaSet(schemaSetName).anchor(anchorName).payload(payload).build()

    def setup() {
        objectUnderTest.networkDataRepository = mockNetworkDataRepository
    }

    def 'Add network data in timeseries database.'() {
        when: 'a new network data is added'
            def result = objectUnderTest.addNetworkData(networkData)
        then: ' repository service is called with the correct parameters'
            1 * mockNetworkDataRepository.save(networkData)
    }

}
