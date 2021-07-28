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

package org.onap.cps.temporal.repository

import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.domain.SearchCriteria
import org.onap.cps.temporal.repository.containers.TimescaleContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.spock.Testcontainers
import org.springframework.test.annotation.Rollback
import spock.lang.Shared
import spock.lang.Specification
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Test specification for network data repository.
 */
@Testcontainers
@DataJpaTest
@Rollback(false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NetworkDataRepositoryImplSpec extends Specification {

    static final String RELOAD_DATA_FOR_SEARCHING = '/data/network-data-changes.sql'

    @Shared
    DateTimeFormatter ISO_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss.SSS')

    def queryDataspaceName = 'DATASPACE-01'
    @Shared
    def querySchemaSetName = 'SCHEMA-SET-01'
    @Shared
    def queryAnchorName = 'ANCHOR-01'

    @Shared
    def observedAsc = new Sort.Order(Sort.Direction.ASC, 'observed_timestamp')
    @Shared
    def observedDesc = new Sort.Order(Sort.Direction.DESC, 'observed_timestamp')
    @Shared
    def anchorAsc = new Sort.Order(Sort.Direction.ASC, 'anchor')

    @Autowired
    NetworkDataRepository networkDataRepository

    @Shared
    TimescaleContainer databaseTestContainer = TimescaleContainer.getInstance()

    @Sql([RELOAD_DATA_FOR_SEARCHING])
    def 'Query: pagination for #scenario'() {
        given: 'search criteria'
            def searchCriteria = (new SearchCriteria.Builder())
                .dataspaceName(queryDataspaceName)
                .anchorName(queryAnchorName)
                .pagination(pageNumber, 1)
                .build()

        when: 'data is fetched'
            Slice<NetworkData> result = networkDataRepository.findBySearchCriteria(searchCriteria)
        then: 'result has expected values'
            result.getNumberOfElements() == 1L
            NetworkData networkData = result.getContent().get(0);
            networkData.getAnchor() == queryAnchorName
            networkData.getDataspace() == queryDataspaceName
            networkData.getSchemaSet() == querySchemaSetName
        and: ' correct pagination details'
            result.hasNext() ? result.nextPageable() : null == expectedNextPageable
            result.hasPrevious() ? result.previousPageable() : null == expectedPreviousPageable
        where:
            scenario      | pageNumber || expectedPreviousPageable | expectedNextPageable
            'first Page'  | 0          || null                     | PageRequest.of(1, 1)
            'middle Page' | 1          || PageRequest.of(0, 1)     | PageRequest.of(2, 1)
            'last Page'   | 2          || PageRequest.of(1, 1)     | null
    }

    @Sql([RELOAD_DATA_FOR_SEARCHING])
    def 'Query: filter by observed after.'() {
        given: 'observed after date'
            def observedAfter = getOffsetDateDate('2021-07-22 01:00:01.000')
        and: 'search criteria'
            def searchCriteria = (new SearchCriteria.Builder())
                .dataspaceName(queryDataspaceName)
                .anchorName(queryAnchorName)
                .observedAfter(observedAfter)
                .pagination(0, 4)
                .build()

        when: 'data is fetched'
            Slice<NetworkData> result = networkDataRepository.findBySearchCriteria(searchCriteria)
        then: 'result have expected number of record'
            result.getNumberOfElements() == 2L
        and: 'each record has observed timestamp on or after the provided value'
            for (NetworkData data : result.getContent()) {
                assert data.getObservedTimestamp().isAfter(observedAfter) || data.getObservedTimestamp().isEqual(observedAfter)
                assert data.getAnchor() == queryAnchorName
                assert data.getDataspace() == queryDataspaceName
            }
    }

    @Sql([RELOAD_DATA_FOR_SEARCHING])
    def 'Query: filter by created before.'() {
        given: 'created before date'
            def createdBefore = getOffsetDateDate('2021-07-22 23:00:01.000')
        and: 'search criteria'
            def searchCriteria = (new SearchCriteria.Builder())
                .dataspaceName(queryDataspaceName)
                .anchorName(queryAnchorName)
                .createdBefore(createdBefore)
                .pagination(0, 4)
                .build()

        when: 'data is fetched'
            Slice<NetworkData> result = networkDataRepository.findBySearchCriteria(searchCriteria)
        then: 'result have expected number of record'
            result.getNumberOfElements() == 2L
        and: 'each record has observed timestamp on or after the provided value'
            for (NetworkData data : result.getContent()) {
                assert data.getCreatedTimestamp().isBefore(createdBefore) || data.getCreatedTimestamp().isEqual(createdBefore)
                assert data.getAnchor() == queryAnchorName
                assert data.getDataspace() == queryDataspaceName
            }
    }

    @Sql([RELOAD_DATA_FOR_SEARCHING])
    def 'Query: sort data by #scenario.'() {
        given: 'search criteria'
            def searchCriteria = (new SearchCriteria.Builder())
                .dataspaceName(queryDataspaceName)
                .schemaSetName(querySchemaSetName)
                .sort(sortOrder)
                .pagination(0, 4)
                .build()

        when: 'data is fetched'
            Slice<NetworkData> result = networkDataRepository.findBySearchCriteria(searchCriteria)
        then: 'result has expected values'
            result.getNumberOfElements() == 4L
            with(result.getContent().get(0)) {
                dataspace == queryDataspaceName
                schemaSet == querySchemaSetName
                anchor == expectedAnchorName
                observedTimestamp == getOffsetDateDate(expectedObservedTimestamp)
            }
        where:
            scenario                      | sortOrder                        || expectedObservedTimestamp | expectedAnchorName
            'observed timestamp asc'      | Sort.by(observedAsc)             || '2021-07-22 00:00:01.000' | 'ANCHOR-01'
            'observed timestamp asc'      | Sort.by(observedDesc)            || '2021-07-24 00:00:01.000' | 'ANCHOR-02'
            'anchor asc, ' +
                'observed timestamp desc' | Sort.by(anchorAsc, observedDesc) || '2021-07-23 00:00:01.000' | 'ANCHOR-01'

    }

    @Sql([RELOAD_DATA_FOR_SEARCHING])
    def 'Query: filter by payload.'() {
        def dataspaceName = 'DATASPACE-02'
        given: 'search criteria'
            def searchCriteria = (new SearchCriteria.Builder())
                .dataspaceName(dataspaceName)
                .schemaSetName(querySchemaSetName)
                .simplePayloadFilter(simplePayloadFilter)
                .pagination(0, 4)
                .build()

        when: 'data is fetched'
            Slice<NetworkData> result = networkDataRepository.findBySearchCriteria(searchCriteria)
        then: 'result has expected values'
            result.getNumberOfElements() == expectedRecordsCount
            with(result.getContent().get(0)) {
                dataspace == dataspaceName
                schemaSet == querySchemaSetName
                anchor == queryAnchorName
            }
        where:
            simplePayloadFilter                    || expectedRecordsCount
            '{"interfaces": [{"id": "01"}]}'       || 2L
            '{"interfaces": [{"status": "down"}]}' || 1L

    }

    OffsetDateTime getOffsetDateDate(String dateTimeString) {
        def localDateTime = LocalDateTime.parse(dateTimeString, ISO_TIMESTAMP_FORMATTER)
        def localZoneOffset = ZoneOffset.systemDefault().getRules().getOffset(localDateTime)
        return OffsetDateTime.of(localDateTime, localZoneOffset)
    }
}
