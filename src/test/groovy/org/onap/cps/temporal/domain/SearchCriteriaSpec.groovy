/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
package org.onap.cps.temporal.domain

import org.springframework.data.domain.Sort
import spock.lang.Specification
import java.time.OffsetDateTime

class SearchCriteriaSpec extends Specification {

    def myDataspace = 'my-dataspace'
    def myAnchorName = 'my-anchor'
    def myschemaSetName = 'my-schemaset'


    def 'Search Criteria has default values if not provided.'() {
        def myPayloadFilter = '{"status": "down"}'
        when: 'search criteria is created'
            def searchCriteria = SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .schemaSetName(myschemaSetName)
                .pagination(0, 10)
                .simplePayloadFilter(myPayloadFilter)
                .build()

        then: 'search criteria has default value for sort'
            searchCriteria.getPageable().getSort() == Sort.by(Sort.Direction.DESC, 'observed_timestamp')
        and: 'created before has almost current time as default value'
            OffsetDateTime.now().minusMinutes(5).isBefore(searchCriteria.getCreatedBefore())
        and: 'contains the provided value to builder'
            searchCriteria.getDataspaceName() == myDataspace
            searchCriteria.getSchemaSetName() == myschemaSetName
            searchCriteria.getSimplePayloadFilter() == myPayloadFilter
            searchCriteria.getPageable().getPageNumber() == 0
            searchCriteria.getPageable().getPageSize() == 10

    }

    def 'Search Criteria with the provided values.'() {

        given: 'sort by parameter'
            def sortBy = Sort.by(Sort.Direction.DESC, 'observed_timestamp')
        and: 'data created one day ago'
            def lastDayAsCreatedBefore = OffsetDateTime.now().minusDays(1)
        and: 'observed timestamp'
            def nowAsObservedAfter = OffsetDateTime.now()
        and: 'simple payload filter'
            def simplePayloadFilter = '{"message":"hello world"}'

        when: 'search criteria is created'
            def searchCriteria = SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .schemaSetName(myschemaSetName)
                .anchorName(myAnchorName)
                .pagination(0, 10)
                .simplePayloadFilter(simplePayloadFilter)
                .sort(sortBy)
                .observedAfter(nowAsObservedAfter)
                .createdBefore(lastDayAsCreatedBefore)
                .build()

        then: 'search criteria has expected value'
            with(searchCriteria) {
                dataspaceName == myDataspace
                schemaSetName == myschemaSetName
                anchorName == myAnchorName
                observedAfter == nowAsObservedAfter
                createdBefore == lastDayAsCreatedBefore
                it.simplePayloadFilter == simplePayloadFilter
                pageable.getPageNumber() == 0
                pageable.getPageSize() == 10
                pageable.getSort() == sortBy
            }
    }

    def 'Error handling: missing dataspace.'() {
        when: 'search criteria is created without dataspace'
            SearchCriteria.builder()
                .anchorName(myAnchorName)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error handling: missing both schemaset and anchor.'() {
        when: 'search criteria is created without schemaset and anchor'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error handling: missing pagination.'() {
        when: 'search criteria is created without pagination'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .anchorName(myAnchorName)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error Handling: sort based on #scenario .'() {
        when: 'search criteria is created without sorting information'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .anchorName(myAnchorName)
                .pagination(0, 1)
                .sort(sort)
                .build()
        then: 'exception is thrown'
            def illegalArgumentException = thrown(IllegalArgumentException)
            def message = illegalArgumentException.getMessage();
            assert message.contains("sort")
            assert message.contains(expectedExceptionMessage)
        where:
            scenario                 | sort                                       | expectedExceptionMessage
            'null'                   | null                                       | "null"
            'unsupported properties' | Sort.by(Sort.Direction.ASC, 'unsupported') | "Invalid sorting"
    }

    def 'Error Handling: Invalid simple payload filter.'() {
        given: 'invalid simple payload filter'
            def inavlidSimplePayloadFilter = 'invalid-json'
        when: 'search criteria is created without invalid simple payload filter'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .anchorName(myAnchorName)
                .pagination(0, 1)
                .simplePayloadFilter(inavlidSimplePayloadFilter)
                .build()
        then: 'exception is thrown'
            thrown(IllegalArgumentException)
    }

}
