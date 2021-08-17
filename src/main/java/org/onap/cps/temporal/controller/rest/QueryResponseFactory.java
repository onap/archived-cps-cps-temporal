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

package org.onap.cps.temporal.controller.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;
import org.onap.cps.temporal.controller.rest.model.AnchorDetails;
import org.onap.cps.temporal.controller.rest.model.AnchorDetailsMapper;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.onap.cps.temporal.controller.rest.model.SortMapper;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.utils.DateTimeUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class QueryResponseFactory {

    private SortMapper sortMapper;
    private String basePath;
    private AnchorDetailsMapper anchorDetailsMapper;

    /**
     * Constructor.
     *
     * @param sortMapper          sortMapper
     * @param anchorDetailsMapper anchorDetailsMapper
     * @param basePath            basePath
     */
    public QueryResponseFactory(final SortMapper sortMapper,
        final AnchorDetailsMapper anchorDetailsMapper,
        @Value("${rest.api.base-path}") final String basePath) {
        this.sortMapper = sortMapper;
        this.anchorDetailsMapper = anchorDetailsMapper;
        this.basePath = basePath;
    }

    /**
     * Use search criteria and search result-set to create response.
     *
     * @param searchCriteria searchCriteria
     * @param searchResult   searchResult
     * @return AnchorHistory
     */
    public AnchorHistory createAnchorsDataByFilterResponse(final SearchCriteria searchCriteria,
        final Slice<NetworkData> searchResult) {

        final var anchorHistory = new AnchorHistory();
        if (searchResult.hasNext()) {
            anchorHistory.setNextRecordsLink(
                toRelativeLink(getAbsoluteLinkForGetAnchorsDataByFilter(searchCriteria, searchResult.nextPageable())));
        }
        if (searchResult.hasPrevious()) {
            anchorHistory.setPreviousRecordsLink(
                toRelativeLink(
                    getAbsoluteLinkForGetAnchorsDataByFilter(searchCriteria, searchResult.previousPageable())));
        }
        anchorHistory.setRecords(convertToAnchorDetails(searchResult.getContent()));
        return anchorHistory;
    }

    /**
     * Use search criteria and search result-set to create response.
     *
     * @param searchCriteria searchCriteria
     * @param searchResult   searchResult
     * @return AnchorHistory
     */
    public AnchorHistory createAnchorDataByNameResponse(final SearchCriteria searchCriteria,
        final Slice<NetworkData> searchResult) {

        final var anchorHistory = new AnchorHistory();
        if (searchResult.hasNext()) {
            anchorHistory.setNextRecordsLink(toRelativeLink(
                getAbsoluteLinkForGetAnchorDataByName(searchCriteria, searchResult.nextPageable())));
        }
        if (searchResult.hasPrevious()) {
            anchorHistory.setPreviousRecordsLink(toRelativeLink(
                getAbsoluteLinkForGetAnchorDataByName(searchCriteria, searchResult.previousPageable())));
        }
        anchorHistory.setRecords(convertToAnchorDetails(searchResult.getContent()));
        return anchorHistory;
    }

    private List<AnchorDetails> convertToAnchorDetails(final List<NetworkData> networkDataList) {
        return networkDataList.stream()
            .map(networkData -> anchorDetailsMapper.toAnchorDetails(networkData))
            .collect(Collectors.toList());
    }

    /*
    Spring hateoas only provides absolute link. But in the microservices, relative links will be more appropriate
     */
    private String toRelativeLink(final String absoluteLink) {

        /* Spring hateoas Issue:
            It does replace the variable defined at the Controller level,
            so we are removing the variable name and replace it with basePath.
            https://github.com/spring-projects/spring-hateoas/issues/361
            https://github.com/spring-projects/spring-hateoas/pull/1375
         */
        final int contextPathBeginIndex = absoluteLink.indexOf("rest.api.base-path%257D");
        return basePath + absoluteLink.substring(contextPathBeginIndex + 23);
    }

    private String getAbsoluteLinkForGetAnchorDataByName(final SearchCriteria searchCriteria,
        final Pageable pageable) {
        final var uriComponentsBuilder = linkTo(methodOn(QueryController.class).getAnchorDataByName(
            searchCriteria.getDataspaceName(),
            searchCriteria.getAnchorName(),
            DateTimeUtility.offsetDateTimeAsString(searchCriteria.getObservedAfter()),
            null,
            DateTimeUtility.offsetDateTimeAsString(searchCriteria.getCreatedBefore()),
            pageable.getPageNumber(), pageable.getPageSize(),
            sortMapper.sortAsString(searchCriteria.getPageable().getSort())))
            .toUriComponentsBuilder();
        addSimplePayloadFilter(uriComponentsBuilder, searchCriteria.getSimplePayloadFilter());
        return uriComponentsBuilder.toUriString();
    }

    private String getAbsoluteLinkForGetAnchorsDataByFilter(final SearchCriteria searchCriteria,
        final Pageable pageable) {
        final var uriComponentsBuilder = linkTo(methodOn(QueryController.class).getAnchorsDataByFilter(
            searchCriteria.getDataspaceName(),
            searchCriteria.getSchemaSetName(),
            DateTimeUtility.offsetDateTimeAsString(searchCriteria.getObservedAfter()),
            null,
            DateTimeUtility.offsetDateTimeAsString(searchCriteria.getCreatedBefore()),
            pageable.getPageNumber(), pageable.getPageSize(),
            sortMapper.sortAsString(searchCriteria.getPageable().getSort())))
            .toUriComponentsBuilder();
        addSimplePayloadFilter(uriComponentsBuilder, searchCriteria.getSimplePayloadFilter());
        return uriComponentsBuilder.toUriString();
    }

    /*
        Spring hateoas does double encoding when generting URI.
        To avoid it in the case of simplePayloadFilter,
         the 'simplePayloadFilter is being added explicitly to UriComponentsBuilder
     */
    private UriComponentsBuilder addSimplePayloadFilter(final UriComponentsBuilder uriComponentsBuilder,
        final String simplePayloadFilter) {
        if (simplePayloadFilter != null) {
            uriComponentsBuilder.queryParam("simplePayloadFilter", simplePayloadFilter);
        }
        return uriComponentsBuilder;
    }
}
