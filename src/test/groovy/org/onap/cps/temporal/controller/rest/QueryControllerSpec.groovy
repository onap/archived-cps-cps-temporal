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

import org.onap.cps.temporal.controller.rest.model.AnchorDetails
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
import org.springframework.data.domain.PageRequest
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

@WebMvcTest(QueryController)
@Import([SortMapper, QueryResponseFactory, AnchorDetailsMapperImpl])
class QueryControllerSpec extends Specification {

    @SpringBean
    NetworkDataService mockNetworkDataService = Mock()

    @Autowired
    MockMvc mvc

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

        def controllerDataBuilder = new QueryControllerDataBuilder(endpointName,
            [dataspace: myDataspace] << urlSpecifParams)
        given: 'network data to be returned'
            def networkData = createNetworkData()
        when: 'endpoint is called without pageNumber, pageLimit, sort and pointInTime'
            def requestBuilder = controllerDataBuilder.
                createMockHttpRequestBuilder();
            def response = mvc.perform(requestBuilder).andReturn().response
        then: 'pageNumber, pageSize and sort has default values'
            interaction {
                def expectedPageable = PageRequest.of(0, 1000,
                    Sort.by(Sort.Order.desc('observed_timestamp')))
                1 * mockNetworkDataService.searchNetworkData(_ as SearchCriteria) >> {
                    SearchCriteria searchCriteria ->
                        assert searchCriteria.getPageable() == expectedPageable
                        assert searchCriteria.getObservedAfter() == null
                        assert searchCriteria.getCreatedBefore().isAfter(OffsetDateTime.now().minusMinutes(2))
                        return new SliceImpl([networkData], searchCriteria.getPageable(), false)
                }
            }
        and: 'response is ok'
            response.getStatus() == HttpStatus.OK.value()
            def anchorHistory = objectMapper.readValue(response.getContentAsString(), AnchorHistory)
        and: 'content has expected values'
            anchorHistory.getPreviousRecordsLink() == null
            anchorHistory.getNextRecordsLink() == null
            anchorHistory.getRecords() == List.of(toAnchorDetails(networkData))
        where:
            endpointName           | urlSpecifParams
            'anchor by name'       | [anchor: myAnchor]
            'anchors by schemaset' | [schemaSet: mySchemaset]
    }

    def 'Get #endpointName: query data #scenario'() {
        def inputParameters = [
            dataspace   : myDataspace,
            pointInTime : '2021-07-24T01:00:01.000-0400',
            pageNumber  : 2, pageLimit: 10,
            sortAsString: 'observed_timestamp:desc']
        inputParameters << urlSpecifParams
        inputParameters << parameters
        def controllerDataBuilder = new QueryControllerDataBuilder(endpointName, inputParameters)
        given:
            def searchCriteria = controllerDataBuilder.createSearchCriteriaBuilder().build()
            def networkData = createNetworkData()
            mockNetworkDataService.searchNetworkData(searchCriteria) >> new SliceImpl<NetworkData>(
                List.of(networkData), searchCriteria.getPageable(), true)
        when: 'endpoint is called with all parameters'
            def requestBuilder = controllerDataBuilder.createMockHttpRequestBuilder()
            def response = mvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)).andReturn().response
            def responseBody = objectMapper.readValue(response.getContentAsString(), AnchorHistory)
        then: 'status is ok'
            response.getStatus() == HttpStatus.OK.value()
        and: 'next and previous record links have expected value'
            controllerDataBuilder.isExpectedNextRecordsLink(responseBody.getNextRecordsLink())
            controllerDataBuilder.isExpectedPreviousRecordsLink(responseBody.getPreviousRecordsLink())
        and: 'has expected network data records'
            responseBody.getRecords().size() == 1
            responseBody.getRecords() == [toAnchorDetails(networkData)]
        where:
            scenario                                                   | endpointName           | urlSpecifParams          | parameters
            'without observedTimestampAfter and with payloadFilter'    | 'anchor by name'       | [anchor: myAnchor]       | [observedTimestampAfter: null, payloadFilter: null]
            'with observedTimestampAfter and without payloadFilter'    | 'anchor by name'       | [anchor: myAnchor]       | [observedTimestampAfter: '2021-07-24T03:00:01.000-0400', payloadFilter: null]
            'without observedTimestampAfter and with payloadFilter'    | 'anchor by name'       | [anchor: myAnchor]       | [observedTimestampAfter: null, payloadFilter: '{"message" : "hello world"}']
            'with observedTimestampAfter and with payloadFilter'       | 'anchor by name'       | [anchor: myAnchor]       | [observedTimestampAfter: '2021-07-24T03:00:01.000-0400', payloadFilter: '{"message" : "hello world"}']
            'without observedTimestampAfter and without payloadFilter' | 'anchors by schemaset' | [schemaSet: mySchemaset] | [observedTimestampAfter: null, payloadFilter: null]
            'with observedTimestampAfter and without payloadFilter'    | 'anchors by schemaset' | [schemaSet: mySchemaset] | [observedTimestampAfter: '2021-07-24T03:00:01.000-0400', payloadFilter: null]
            'without observedTimestampAfter and with payloadFilter'    | 'anchors by schemaset' | [schemaSet: mySchemaset] | [observedTimestampAfter: null, payloadFilter: '{"message" : "hello world"}']
            'with observedTimestampAfter and with payloadFilter'       | 'anchors by schemaset' | [schemaSet: mySchemaset] | [observedTimestampAfter: '2021-07-24T03:00:01.000-0400', payloadFilter: '{"message" : "hello world"}']
    }

    def 'Get #endpointName: Sort by #sortAsString'() {
        given: 'sort parameters'
            def parameters = [dataspace: myDataspace, sortAsString: sortAsString] << uriSpecificParams
        when: 'endpoint is called'
            def controllerDataBuilder = new QueryControllerDataBuilder(endpointName, parameters)
            def response = mvc.perform(controllerDataBuilder.createMockHttpRequestBuilder())
                .andReturn().response
        then: 'network data service is called with expected sort'
            1 * mockNetworkDataService.searchNetworkData(_ as SearchCriteria) >> {
                SearchCriteria searchCriteria ->
                    assert searchCriteria.getPageable().getSort() == expectedSort
                    return new SliceImpl([], searchCriteria.getPageable(), true)
            }
        and: 'response is ok'
            response.getStatus() == HttpStatus.OK.value()
            def anchorHistory = objectMapper.readValue(response.getContentAsString(), AnchorHistory)
        and: 'content has expected values'
            controllerDataBuilder.isExpectedNextRecordsLink(anchorHistory.getNextRecordsLink())
            anchorHistory.getPreviousRecordsLink() == null
        where:
            endpointName           | uriSpecificParams        | sortAsString                         || expectedSort
            'anchor by name'       | [anchor: myAnchor]       | 'observed_timestamp:desc'            || Sort.by(observedDescSortOrder)
            'anchor by name'       | [anchor: myAnchor]       | 'anchor:asc,observed_timestamp:desc' || Sort.by(anchorAscSortOrder, observedDescSortOrder)
            'anchors by schemaset' | [schemaSet: mySchemaset] | 'observed_timestamp:desc'            || Sort.by(observedDescSortOrder)
            'anchors by schemaset' | [schemaSet: mySchemaset] | 'anchor:asc,observed_timestamp:desc' || Sort.by(anchorAscSortOrder, observedDescSortOrder)
    }

    def 'Get #endpointName Error handling: invalid date format in #queryParamName '() {
        given: 'sort parameters'
            def parameters = [dataspace: myDataspace] << uriSpecificParams
            parameters[queryParamName] = 'invalid-date-string'
        when: 'endpoint is called'
            QueryControllerDataBuilder dataBuilder = new QueryControllerDataBuilder(endpointName, parameters)
            def response = mvc.perform(dataBuilder.createMockHttpRequestBuilder())
                .andReturn().response
        then: 'received bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains(queryParamName)
            errorMessage.getMessage().contains("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        where:
            endpointName           | uriSpecificParams        | queryParamName
            'anchor by name'       | [anchor: myAnchor]       | 'pointInTime'
            'anchor by name'       | [anchor: myAnchor]       | 'observedTimestampAfter'
            'anchors by schemaset' | [schemaSet: mySchemaset] | 'pointInTime'
            'anchors by schemaset' | [schemaSet: mySchemaset] | 'observedTimestampAfter'
    }

    def 'Get #endpointName Error handling: invalid sort format #scenario'() {
        given: 'sort parameters'
            def parameters = [dataspace: myDataspace, sortAsString: sortAsString] << uriSpecificParams
        when: 'endpoint is called'
            def controllerDataBuilder = new QueryControllerDataBuilder(endpointName, parameters)
            def response = mvc.perform(controllerDataBuilder.createMockHttpRequestBuilder())
                .andReturn().response
        then: 'received bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains('sort')
            errorMessage.getMessage().contains('<fieldname>:<direction>,...,<fieldname>:<direction>')
        where:
            scenario            | sortAsString             | endpointName           | uriSpecificParams
            'missing direction' | 'observed_timestamp'     | 'anchor by name'       | [anchor: myAnchor]
            'missing separator' | 'observed_timestampdesc' | 'anchor by name'       | [anchor: myAnchor]
            'missing direction' | 'observed_timestamp'     | 'anchors by schemaset' | [schemaSet: mySchemaset]
            'missing separator' | 'observed_timestampdesc' | 'anchors by schemaset' | [schemaSet: mySchemaset]
    }

    def 'Get #endpointName Error handling: invalid simple payload filter '() {
        given: 'payload filter parameters'
            def parameters = [dataspace: myDataspace, payloadFilter: 'invalid-json'] << uriSpecificParams
        when: 'endpoint is called'
            def controllerDataBuilder = new QueryControllerDataBuilder(endpointName, parameters)
            def response = mvc.perform(controllerDataBuilder.createMockHttpRequestBuilder())
                .andReturn().response
        then: 'received bad request status'
            response.getStatus() == HttpStatus.BAD_REQUEST.value()
        and: 'error details'
            def errorMessage = objectMapper.readValue(response.getContentAsString(), ErrorMessage)
            errorMessage.getStatus() == HttpStatus.BAD_REQUEST.value().toString()
            errorMessage.getMessage().contains('simplePayloadFilter')
        where: 'endpoints are provided'
            endpointName           | uriSpecificParams
            'anchor by name'       | [anchor: myAnchor]
            'anchors by schemaset' | [schemaSet: mySchemaset]
    }

    NetworkData createNetworkData() {
        return NetworkData.builder().dataspace(myDataspace)
            .schemaSet(mySchemaset).anchor(myAnchor).payload('{"message" : "Hello World"}')
            .observedTimestamp(OffsetDateTime.now())
            .createdTimestamp(OffsetDateTime.now()).build()
    }

    AnchorDetails toAnchorDetails(NetworkData networkData) {
        AnchorDetails anchorDetails = new AnchorDetails()
        anchorDetails.setDataspace(networkData.getDataspace())
        anchorDetails.setAnchor(networkData.getAnchor())
        anchorDetails.setSchemaSet(networkData.getSchemaSet())
        anchorDetails.setObservedTimestamp(DateTimeUtility.offsetDateTimeAsString(networkData.getObservedTimestamp()))
        anchorDetails.setData(networkData.getPayload())
        return anchorDetails
    }
}
