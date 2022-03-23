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

import org.onap.cps.temporal.controller.utils.DateTimeUtility
import org.onap.cps.temporal.domain.SearchCriteria
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.CollectionUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils

import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.OffsetDateTime

/*
To create objects required for the test based on same input
 */

class QueryControllerDataBuilder {

    private static String POINT_IN_TIME_QUERY_PARAM = 'pointInTime'
    private static String OBSERVED_TIMESTAMP_AFTER_QUERY_PARAM = 'observedTimestampAfter'
    private static String PAGE_NUMBER_QUERY_PARAM = 'pageNumber'
    private static String PAGE_LIMIT_QUERY_PARAM = 'pageLimit'
    private static String SORT_QUERY_PARAM = 'sort'
    private static String SIMPLE_PAYLOAD_FILTER_QUERY_PARAM = 'simplePayloadFilter'

    private static int DEFAULT_PAGE_NUMBER = 0
    private static int DEFAULT_PAGE_SIZE = 1000
    private static String DEFAULT_SORT = 'observed_timestamp:desc'

    private static Map SORT_MAP = ['anchor:asc'             : Sort.by(Sort.Direction.ASC, 'anchor'),
                                   'observed_timestamp:desc': Sort.by(Sort.Direction.DESC, 'observed_timestamp')]
    private static Map URI_MAP =
        ['anchor by name'      : '/cps-temporal/api/v1/dataspaces/{dataspace}/anchors/{anchor}/history',
         'anchors by schemaset': '/cps-temporal/api/v1/dataspaces/{dataspace}/anchors/history?schema-set-name={schemaSet}']

    Map parameters
    String endpoint

    QueryControllerDataBuilder(final String endPointName, final Map parameters) {
        this.parameters = parameters
        def replacements = ['{dataspace}': parameters.dataspace,
                            '{schemaSet}': parameters.schemaSet,
                            '{anchor}'   : parameters.anchor]
        endpoint = URI_MAP.get(endPointName).replace(replacements)
    }

    MockHttpServletRequestBuilder createMockHttpRequestBuilder() {
        def requestBuilder = MockMvcRequestBuilders.get(endpoint)
        if (parameters.pointInTime != null)
            requestBuilder.queryParam(POINT_IN_TIME_QUERY_PARAM, parameters.pointInTime)
        if (parameters.observedTimestampAfter != null)
            requestBuilder.queryParam(OBSERVED_TIMESTAMP_AFTER_QUERY_PARAM, parameters.observedTimestampAfter)
        if (parameters.pageNumber != null)
            requestBuilder.queryParam(PAGE_NUMBER_QUERY_PARAM, parameters.pageNumber.toString())
        if (parameters.pageLimit != null)
            requestBuilder.queryParam(PAGE_LIMIT_QUERY_PARAM, parameters.pageLimit.toString())
        if (parameters.sortAsString != null)
            requestBuilder.queryParam(SORT_QUERY_PARAM, parameters.sortAsString)
        if (parameters.payloadFilter != null)
            requestBuilder.queryParam(SIMPLE_PAYLOAD_FILTER_QUERY_PARAM, parameters.payloadFilter)
        return requestBuilder.contentType(MediaType.APPLICATION_JSON)
    }

    SearchCriteria.Builder createSearchCriteriaBuilder() {
        def searchCriteriaBuilder = SearchCriteria.builder()
        searchCriteriaBuilder.dataspaceName(parameters.dataspace)
            .anchorName(parameters.anchor)
            .schemaSetName(parameters.schemaSet)
        if (parameters.pointInTime != null)
            searchCriteriaBuilder.createdBefore(DateTimeUtility.toOffsetDateTime(parameters.pointInTime))
        if (parameters.observedTimestampAfter != null)
            searchCriteriaBuilder.observedAfter(DateTimeUtility.toOffsetDateTime(parameters.observedTimestampAfter))
        if (parameters.pageNumber != null)
            searchCriteriaBuilder.pagination(parameters.pageNumber, parameters.pageLimit)
        if (parameters.payloadFilter != null)
            searchCriteriaBuilder.simplePayloadFilter(parameters.payloadFilter)
        if (parameters.sortAsString != null)
            searchCriteriaBuilder.sort(SORT_MAP.get(((String) parameters.sortAsString).toLowerCase()))
        return searchCriteriaBuilder
    }

    private int getPageNumber() {
        return parameters.pageNumber == null ?
            DEFAULT_PAGE_NUMBER :
            parameters.pageNumber
    }

    void isExpectedNextRecordsLink(String actualNextLink) {
        isExpectedLink(getPageNumber() + 1, actualNextLink)
    }

    void isExpectedPreviousRecordsLink(String actualNextLink) {
        isExpectedLink(getPageNumber() - 1, actualNextLink)
    }

    void isExpectedLink(int pageNumber, String actualLink) {
        def actualUriComponents = UriComponentsBuilder.fromUriString(actualLink).build()
        def actualQueryParams = actualUriComponents.getQueryParams()

        if (parameters.observedTimestampAfter != null) {
            validateQueryParam(OBSERVED_TIMESTAMP_AFTER_QUERY_PARAM, parameters.observedTimestampAfter, actualQueryParams)
        }
        if (parameters.payloadFilter != null) {
            validateQueryParam(SIMPLE_PAYLOAD_FILTER_QUERY_PARAM, parameters.payloadFilter, actualQueryParams)
        }
        validatePointInTime(actualQueryParams)
        validateQueryParam(PAGE_NUMBER_QUERY_PARAM, Integer.toString(pageNumber), actualQueryParams)
        validateQueryParam(PAGE_LIMIT_QUERY_PARAM,
            Integer.toString(parameters.pageLimit == null ? DEFAULT_PAGE_SIZE : parameters.pageLimit), actualQueryParams)
        validateQueryParam(SORT_QUERY_PARAM,
            parameters.sortAsString == null ? DEFAULT_SORT : parameters.sortAsString, actualQueryParams)

    }

    private void validateQueryParam(String paramName, Object expectedValue, MultiValueMap<String, String> queryParams) {
        def values = queryParams.get(paramName)
        assert (!CollectionUtils.isEmpty(values))

        def actualValue =  URLDecoder.decode(values.get(0), Charset.defaultCharset())
        actualValue = actualValue
            .replaceAll('%','')
            .replaceAll('253A', ':')
            .replaceAll('252B', '+')
            .replaceAll('252C', ',')
        assert (expectedValue == actualValue)
    }

    boolean validatePointInTime(MultiValueMap<String, String> queryParams) {

        def values = queryParams.get(POINT_IN_TIME_QUERY_PARAM)
        assert (!CollectionUtils.isEmpty(values))
        def actualValue = URLDecoder.decode(values.get(0), Charset.defaultCharset())
        actualValue = actualValue.replaceAll('%','').replaceAll('253A', ':')
            .replaceAll('252B', '+')
        if (parameters.pointInTime == null) {
            assert DateTimeUtility.toOffsetDateTime(actualValue).isAfter(OffsetDateTime.now().minusMinutes(2))
        } else {
            assert parameters.pointInTime == actualValue
        }
    }

}
