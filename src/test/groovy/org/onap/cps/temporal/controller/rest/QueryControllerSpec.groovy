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

package org.onap.cps.temporal.controller.rest

import org.onap.cps.temporal.controller.rest.model.AnchorDetailsMapperImpl
import org.onap.cps.temporal.controller.rest.model.AnchorHistory
import org.onap.cps.temporal.controller.rest.model.ErrorMessage
import org.onap.cps.temporal.controller.rest.model.SortMapper
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.domain.SearchCriteria
import org.onap.cps.temporal.service.NetworkDataService
import org.onap.cps.temporal.utils.DateTimeUtility
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared

import java.time.OffsetDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import spock.lang.Specification

/**
 * Specification for Query Controller.
 */
@WebMvcTest(QueryController)
@Import([SortMapper, QueryResponseFactory, AnchorDetailsMapperImpl])
class QueryControllerSpec extends Specification {

    @SpringBean
    NetworkDataService mockNetworkDataService = Mock()

    @Autowired
    MockMvc mvc

    //@Value('${rest.api.base-path}')
    @Shared
    def basePath = "/cps-temporal/api"
    @Shared
    def myDataspace = 'my-dataspace'
    @Shared
    def myAnchor = 'my-anchor'
    @Shared
    def mySchemaset = 'my-schemaset'
    @Shared
    def objectMapper = new ObjectMapper()

    @Shared
    def observedDescSortOrder = new Sort.Order(Sort.Direction.DESC, 'observed_timestamp')
    @Shared
    def anchorAscSortOrder = new Sort.Order(Sort.Direction.ASC, 'anchor')

    def 'Get #endpointName: default values if missing'() {
        when: 'endpoint is called without pageNumber, pageLimit, sort and pointInTime'
            def response = mvc.perform(get(endpoint)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then: 'pageNumber, pageSize and sort has default values'
            1 * mockNetworkDataService.searchNetworkData(_ as SearchCriteria) >> {
                SearchCriteria searchCriteria ->
                    assert searchCriteria.getPageable().getPageSize() == 1000
                    assert searchCriteria.getPageable().getPageNumber() == 0
                    assert searchCriteria.getPageable().getSort() ==
                        Sort.by(Sort.Order.desc('observed_timestamp'))
                    assert searchCriteria.getCreatedBefore().isAfter(OffsetDateTime.now().minusMinutes(2))
                    return new SliceImpl([], searchCriteria.getPageable(), true)
            }
        where:
            endpointName           | endpoint
            'anchor by name'       | getAnchorByNameEndpoint()
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint()
    }

    def 'Get #endpointName: query data with provided values'() {
        def inputParamaters = [
            pointInTime  : '2021-07-24T01:00:01.000-0400',
            observedAfter: observedTimestamp,
            pageNumber   : 2, pageLimit: 10,
            sortAsString : 'anchor:ASC',
            payloadFilter: payloadFilter]
        inputParamaters << urlSpecifParams
        def controllerFixtures = new ControllerFixtures(myDataspace, inputParamaters)
        given:
            def searchCriteria = controllerFixtures.getSearchCriteriaBuilder().sort(Sort.by(anchorAscSortOrder)).build()
            def networkData = NetworkData.builder().dataspace(myDataspace)
                .schemaSet(mySchemaset).anchor(myAnchor).payload(payloadFilter)
                .observedTimestamp(DateTimeUtility.getOffsetDateTime(observedTimestamp))
                .createdTimestamp(OffsetDateTime.now()).build()
            mockNetworkDataService.searchNetworkData(searchCriteria) >> new SliceImpl<NetworkData>(
                List.of(networkData), searchCriteria.getPageable(), true)

        when: 'endpoint is called with all parameters'
            def requestBuilder = get(endpoint);
            controllerFixtures.addQueryParameters(requestBuilder)
            def response = mvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)).andReturn().response
            def responseBody = objectMapper.readValue(response.getContentAsString(), AnchorHistory)
        then: 'status is ok'
            response.getStatus() == HttpStatus.OK.value()
        and: 'next and previous record links have expected value'
            responseBody.getNextRecordsLink() == controllerFixtures.getNextExpectedLink(endpoint)
            responseBody.getPreviousRecordsLink() == controllerFixtures.getPreviousExpectedLink(endpoint)
        and: 'has expected network data records'
            responseBody.getRecords().size() == 1
            with(responseBody.getRecords().get(0)) {
                getDataspace() == myDataspace
                getAnchor() == myAnchor
                getSchemaSet() == mySchemaset
                getData() != null ? objectMapper.writeValueAsString(getData()) : getData() == payloadFilter
                getObservedTimestamp() == observedTimestamp
            }
        where:
            endpointName           | endpoint                        | urlSpecifParams          | observedTimestamp              | payloadFilter
            'anchor by name'       | getAnchorByNameEndpoint()       | [anchor: myAnchor]       | null                           | null
            'anchor by name'       | getAnchorByNameEndpoint()       | [anchor: myAnchor]       | '2021-07-24T03:00:01.000-0400' | null
            'anchor by name'       | getAnchorByNameEndpoint()       | [anchor: myAnchor]       | null                           | '{"message" : "hello world"}'
            'anchor by name'       | getAnchorByNameEndpoint()       | [anchor: myAnchor]       | '2021-07-24T03:00:01.000-0400' | '{"message" : "hello world"}'
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | [schemaSet: mySchemaset] | null                           | null
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | [schemaSet: mySchemaset] | '2021-07-24T03:00:01.000-0400' | null
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | [schemaSet: mySchemaset] | null                           | '{"message" : "hello world"}'
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | [schemaSet: mySchemaset] | '2021-07-24T03:00:01.000-0400' | '{"message" : "hello world"}'
    }

    def 'Get #endpointName: Sort by #sortAsString'() {
        when: 'endpoint is called'
            def response = mvc.perform(get(endpoint).queryParam('sort', sortAsString)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then: 'network data service is called with expected sort'
            1 * mockNetworkDataService.searchNetworkData(_ as SearchCriteria) >> {
                SearchCriteria searchCriteria ->
                    assert searchCriteria.getPageable().getSort() == expectedSort
                    return new SliceImpl([], searchCriteria.getPageable(), false)
            }
        where:
            endpointName           | endpoint                        | sortAsString                         || expectedSort
            'anchor by name'       | getAnchorByNameEndpoint()       | 'observed_timestamp:desc'            || Sort.by(observedDescSortOrder)
            'anchor by name'       | getAnchorByNameEndpoint()       | 'anchor:asc'                         || Sort.by(anchorAscSortOrder)
            'anchor by name'       | getAnchorByNameEndpoint()       | 'anchor:asc,observed_timestamp:desc' || Sort.by(anchorAscSortOrder, observedDescSortOrder)
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | 'observed_timestamp:desc'            || Sort.by(observedDescSortOrder)
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | 'anchor:asc'                         || Sort.by(anchorAscSortOrder)
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | 'anchor:asc,observed_timestamp:desc' || Sort.by(anchorAscSortOrder, observedDescSortOrder)

    }

    def 'Get #endpointName Error handling: invalid date format in #queryParamName '() {
        when: 'endpoint is called'
            def response = mvc.perform(get(endpoint).queryParam(queryParamName, "invalid-date-string")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then: 'recieved bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains(queryParamName)
            errorMessage.getMessage().contains("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        where:
            endpointName           | endpoint                        | queryParamName
            'anchor by name'       | getAnchorByNameEndpoint()       | 'pointInTime'
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint() | 'observedTimestampAfter'
    }

    def 'Get #endpointName Error handling: invalid sort format #scenario'() {
        when: 'endpoint is called'
            def response = mvc.perform(get(endpoint).queryParam('sort', sortAsString)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then: 'recieved bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains('sort')
            errorMessage.getMessage().contains('<fieldname>:<direction>,...,<fieldname>:<direction>')
        where:
            scenario            | sortAsString             | endpointName           | endpoint
            'missing direction' | 'observed_timestamp'     | 'anchor by name'       | getAnchorByNameEndpoint()
            'missing separator' | 'observed_timestampdesc' | 'anchors by schemaset' | getAnchorsBySchemaSetEndpoint()
    }

    def 'Get #endpointName Error handling: invalid simple payload filter '() {
        when: 'endpoint is called'
            def response = mvc.perform(get(endpoint).queryParam('simplePayloadFilter', "invalid-json")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then: 'recieved bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains('simplePayloadFilter')
        where: 'endpoints are provided '
            endpointName           | endpoint
            'anchor by name'       | getAnchorByNameEndpoint()
            'anchors by schemaset' | getAnchorsBySchemaSetEndpoint()
    }

    String getAnchorByNameEndpoint() {
        return "${basePath}/v1/dataspaces/${myDataspace}/anchors/${myAnchor}/history"
    }

    String getAnchorsBySchemaSetEndpoint() {
        return "${basePath}/v1/dataspaces/${myDataspace}/anchors/history?schema-set-name=${mySchemaset}"
    }

}
