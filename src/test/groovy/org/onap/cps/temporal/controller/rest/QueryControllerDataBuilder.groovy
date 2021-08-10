package org.onap.cps.temporal.controller.rest

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder
import org.onap.cps.temporal.domain.SearchCriteria
import org.onap.cps.temporal.utils.DateTimeUtility
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.util.UriComponentsBuilder
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
        println(URI_MAP)
        println(URI_MAP.get(endPointName))
        def replacements = ['{dataspace}': parameters.dataspace,
                            '{schemaSet}': parameters.schemaSet,
                            '{anchor}'   : parameters.anchor]
        println(replacements)
        endpoint = URI_MAP.get(endPointName).replace(replacements)

        println(endpoint)

    }

    MockHttpServletRequestBuilder createMockHttpRequestBuilder() {
        def requestBuilder = MockMvcRequestBuilders.get(endpoint)
        if (parameters.pointInTime != null)
            requestBuilder.queryParam(POINT_IN_TIME_QUERY_PARAM, parameters.pointInTime)
        if (parameters.observedAfter != null)
            requestBuilder.queryParam(OBSERVED_TIMESTAMP_AFTER_QUERY_PARAM, parameters.observedAfter)
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
            searchCriteriaBuilder.createdBefore(DateTimeUtility.getOffsetDateTime(parameters.pointInTime))
        if (parameters.observedAfter != null)
            searchCriteriaBuilder.observedAfter(DateTimeUtility.getOffsetDateTime(parameters.observedAfter))
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

    String createNextExpectedLink() {
        def pageNumber = getPageNumber()
        return getLink(getPageNumber() + 1)
    }

    String createPreviousExpectedLink() {
        return getLink(getPageNumber() - 1)
    }

    private String getLink(int pageNumber) {
        def uriComponentBuilder = UriComponentsBuilder.fromUriString(endpoint)
        if (parameters.observedAfter != null)
            uriComponentBuilder.queryParam(OBSERVED_TIMESTAMP_AFTER_QUERY_PARAM, parameters.observedAfter)
        if (parameters.pointInTime != null)
            uriComponentBuilder.queryParam(POINT_IN_TIME_QUERY_PARAM, parameters.pointInTime)
        uriComponentBuilder.queryParam(PAGE_NUMBER_QUERY_PARAM, pageNumber)
        uriComponentBuilder.queryParam(PAGE_LIMIT_QUERY_PARAM,
            parameters.pageLimit == null ? DEFAULT_PAGE_SIZE : parameters.pageLimit)
        uriComponentBuilder.queryParam(SORT_QUERY_PARAM,
            parameters.sortAsString == null ? DEFAULT_SORT : parameters.sortAsString)
        if (parameters.payloadFilter != null)
            uriComponentBuilder.queryParam(SIMPLE_PAYLOAD_FILTER_QUERY_PARAM, parameters.payloadFilter)
        return uriComponentBuilder.toUriString()
    }

    boolean isExpectedNextRecordsLink(String actualNextLink) {

        if (parameters.pointInTime == null) {
            validateAndSetPointInTime(actualNextLink)
        }
        return createNextExpectedLink() == actualNextLink
    }

    boolean isExpectedPreviousRecordsLink(String actualPreviousLink) {

        if (parameters.pointInTime == null) {
            validateAndSetPointInTime(actualPreviousLink)
        }
        createNextExpectedLink() == actualPreviousLink
    }

    boolean validateAndSetPointInTime(String actualLink) {
        def actualUriComponents = UriComponentsBuilder.fromUriString(actualLink).build()
        def actualQueryParams = actualUriComponents.getQueryParams()
        def pointInTime = actualQueryParams.get(POINT_IN_TIME_QUERY_PARAM).get(0)

        assert DateTimeUtility.getOffsetDateTime(pointInTime).isAfter(OffsetDateTime.now().minusMinutes(2))
        parameters.pointInTime = actualQueryParams.get(POINT_IN_TIME_QUERY_PARAM)
    }

}
