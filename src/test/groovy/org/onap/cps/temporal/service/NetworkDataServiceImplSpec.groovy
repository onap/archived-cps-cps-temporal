/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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
import org.onap.cps.temporal.domain.Operation
import org.onap.cps.temporal.domain.SearchCriteria
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ContextConfiguration

import javax.validation.ValidationException
import java.time.OffsetDateTime
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import spock.lang.Specification

/**
 * Test specification for network data service.
 */
@SpringBootTest
@ContextConfiguration(classes = NetworkDataServiceImpl)
class NetworkDataServiceImplSpec extends Specification {

    @SpringBean
    NetworkDataRepository mockNetworkDataRepository = Mock()

    @Autowired
    NetworkDataService objectUnderTest

    @Value('${app.query.response.max-page-size}')
    int maxPageSize

    def networkData = NetworkData.builder().operation(Operation.UPDATE).payload("{}").build()

    def 'Add network data successfully.'() {
        given: 'a network data'
            def networkData = NetworkData.builder().operation(operation).payload(payload).build()
        and: 'network data repository is persisting network data'
            def persistedNetworkData = new NetworkData()
            persistedNetworkData.setCreatedTimestamp(OffsetDateTime.now())
            mockNetworkDataRepository.save(networkData) >> persistedNetworkData
        when: 'a new network data is added'
            def result = objectUnderTest.addNetworkData(networkData)
        then: 'result network data is the one that has been persisted'
            result == persistedNetworkData
            result.getCreatedTimestamp() != null
            networkData.getCreatedTimestamp() == null
        where: 'the following data is used'
            operation        | payload
            Operation.CREATE | '{ "key" : "value" }'
            Operation.UPDATE | '{ "key" : "value" }'
            Operation.DELETE | null
    }

    def 'Error Handling: Payload missing for #operation'() {
        given: 'a network data'
            def networkData = NetworkData.builder().operation(operation).build()
        when: 'a new network data is added'
            objectUnderTest.addNetworkData(networkData)
        then: 'Validation exception is thrown'
            def exception = thrown(ValidationException)
            exception.getMessage().contains('null payload')
        where: 'following operations are used'
            operation  << [ Operation.CREATE, Operation.UPDATE]
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
            existing.setOperation(Operation.UPDATE)
            existing.setPayload('{}')
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

    def 'Query network data with more than max page-size'() {
        given: 'search criteria with more than max page size'
            def searchCriteria = SearchCriteria.builder()
                    .dataspaceName('my-dataspaceName')
                    .schemaSetName('my-schemaset')
                    .pagination(0, maxPageSize + 1)
                    .build()
        when: 'search is executed'
            objectUnderTest.searchNetworkData(searchCriteria)
        then: 'a validation exception is thrown'
            thrown(ValidationException)
    }

}
