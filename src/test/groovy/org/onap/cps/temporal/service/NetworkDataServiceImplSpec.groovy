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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.service

import org.onap.cps.temporal.domain.NetworkDataId
import org.onap.cps.temporal.domain.SearchCriteria
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import spock.lang.Specification

/**
 * Test specification for network data service.
 */
class NetworkDataServiceImplSpec extends Specification {

    def mockNetworkDataRepository = Mock(NetworkDataRepository)

    def objectUnderTest = new NetworkDataServiceImpl(mockNetworkDataRepository)

    def networkData = new NetworkData()

    def 'Add network data successfully.'() {
        given: 'network data repository is persisting network data it is asked to save'
            def persistedNetworkData = new NetworkData()
            persistedNetworkData.setCreatedTimestamp(OffsetDateTime.now())
            mockNetworkDataRepository.save(networkData) >> persistedNetworkData
        when: 'a new network data is added'
            def result = objectUnderTest.addNetworkData(networkData)
        then: 'result network data is the one that has been persisted'
            result == persistedNetworkData
            result.getCreatedTimestamp() != null
            networkData.getCreatedTimestamp() == null
    }

    def 'Add network data fails because already added'() {
        given:
            'network data repository is not able to create data it is asked to persist ' +
                'and reveals it with null created timestamp on network data entity'
            def persistedNetworkData = new NetworkData()
            persistedNetworkData.setCreatedTimestamp(null)
            mockNetworkDataRepository.save(networkData) >> persistedNetworkData
        and: 'existing data can be retrieved'
            def existing = new NetworkData()
            existing.setCreatedTimestamp(OffsetDateTime.now().minusYears(1))
            mockNetworkDataRepository.findById(_ as NetworkDataId) >> Optional.of(existing)
        when: 'a new network data is added'
            objectUnderTest.addNetworkData(networkData)
        then: 'network service exception is thrown'
            thrown(ServiceException)
    }

    def 'Query network data by search criteria.'() {
        given: 'search criteria'
            def searchCriteria = SearchCriteria.builder()
                .dataspaceName('my-dataspaceName')
                .schemaSetName('my-schemaset')
                .pagination(0, 10)
                .build()
        and: 'response from repository'
            def pageFromRepository = new PageImpl<>(Collections.emptyList(), searchCriteria.getPageable(), 10)
            mockNetworkDataRepository.findBySearchCriteria(searchCriteria) >> pageFromRepository

        when: 'search is executed'
            def resultPage = objectUnderTest.searchNetworkData(searchCriteria)

        then: 'data is fetched from repository and returned'
            resultPage == pageFromRepository

    }

}
