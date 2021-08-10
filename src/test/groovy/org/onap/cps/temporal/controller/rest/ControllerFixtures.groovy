package org.onap.cps.temporal.controller.rest

import org.mockito.Mock
import org.onap.cps.temporal.domain.SearchCriteria
import org.onap.cps.temporal.utils.DateTimeUtility
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

/*
    1. Get corresponding search criteria
    2. Get NetworkData
    3. Load query Params
 */

class ControllerFixtures {

    String dataspace
    String schemaSet
    String anchor
    Map parameters

    ControllerFixtures(String dataspace, final Map parameters) {
        this.dataspace = dataspace
        schemaSet = parameters.schemaSet
        anchor = parameters.anchor
        this.parameters = parameters
    }

    MockHttpServletRequestBuilder addQueryParameters(final MockHttpServletRequestBuilder requestBuilder) {
        if (parameters.pointInTime != null)
            requestBuilder.queryParam('pointInTime', parameters.pointInTime)
        if (parameters.observedAfter != null)
            requestBuilder.queryParam('observedTimestampAfter', parameters.observedAfter)
        if (parameters.pageNumber != null)
            requestBuilder.queryParam('pageNumber', parameters.pageNumber.toString())
        if (parameters.pageLimit != null)
            requestBuilder.queryParam('pageLimit', parameters.pageLimit.toString())
        if (parameters.sortAsString != null)
            requestBuilder.queryParam('sort', parameters.sortAsString)
        if (parameters.payloadFilter != null)
            requestBuilder.queryParam('simplePayloadFilter', parameters.payloadFilter)
        return requestBuilder
    }

    SearchCriteria.Builder getSearchCriteriaBuilder() {
        def searchCriteriaBuilder = SearchCriteria.builder()
        searchCriteriaBuilder.dataspaceName(dataspace)
            .anchorName(anchor)
            .schemaSetName(schemaSet)
        if (parameters.pointInTime != null)
            searchCriteriaBuilder.createdBefore(DateTimeUtility.getOffsetDateTime(parameters.pointInTime))
        if (parameters.observedAfter != null)
            searchCriteriaBuilder.observedAfter(DateTimeUtility.getOffsetDateTime(parameters.observedAfter))
        if (parameters.pageNumber != null)
            searchCriteriaBuilder.pagination(parameters.pageNumber, parameters.pageLimit)
        if (parameters.payloadFilter != null)
            searchCriteriaBuilder.simplePayloadFilter(parameters.payloadFilter)
    }

}
