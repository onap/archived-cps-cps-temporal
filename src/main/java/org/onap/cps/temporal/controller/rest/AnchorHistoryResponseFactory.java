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

import java.util.stream.Collectors;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.utils.DateTimeUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class AnchorHistoryResponseFactory {

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
    public AnchorHistoryResponseFactory(final SortMapper sortMapper,
        final AnchorDetailsMapper anchorDetailsMapper,
        @Value("${rest.api.base-path}") final String basePath) {
        this.sortMapper = sortMapper;
        this.anchorDetailsMapper = anchorDetailsMapper;
        this.basePath = basePath;
    }

    AnchorHistory createAnchorsDataBySchemaSetResponse(final SearchCriteria searchCriteria,
        final Slice<NetworkData> response) {

        final var anchorHistory = new AnchorHistory();
        if (response.hasNext()) {
            anchorHistory.setNextRecordsLink(
                toRelativeLink(getAbsoluteLinkForGetAnchorsDataBySchemaSet(searchCriteria, response.nextPageable())));
        }
        if (response.hasPrevious()) {
            anchorHistory.setPreviousRecordsLink(
                toRelativeLink(
                    getAbsoluteLinkForGetAnchorsDataBySchemaSet(searchCriteria, response.previousPageable())));
        }
        anchorHistory.setRecords(response.getContent().stream()
            .map(networkData -> anchorDetailsMapper.networkDataToAnchorHistory(networkData))
            .collect(Collectors.toList()));
        return anchorHistory;
    }

    AnchorHistory createAnchorDataByNameResponse(final SearchCriteria searchCriteria,
        final Slice<NetworkData> response) {

        final var anchorHistory = new AnchorHistory();
        if (response.hasNext()) {
            anchorHistory.setNextRecordsLink(toRelativeLink(
                getAbsoluteLinkForGetAnchorDataByName(searchCriteria, response.nextPageable())));
        }
        if (response.hasPrevious()) {
            anchorHistory.setPreviousRecordsLink(toRelativeLink(
                getAbsoluteLinkForGetAnchorDataByName(searchCriteria, response.previousPageable())));
        }
        anchorHistory.setRecords(response.getContent().stream()
            .map(networkData -> anchorDetailsMapper.networkDataToAnchorHistory(networkData))
            .collect(Collectors.toList()));
        return anchorHistory;
    }

    private String toRelativeLink(final String absoluteLink) {

        /* Issue with spring hateos, so need to provide the value of absoluteLink
        .replace("${rest.api.base-path}", basePath));
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
        if (searchCriteria.getSimplePayloadFilter() != null) {
            uriComponentsBuilder.queryParam("simplePayloadFilter", searchCriteria.getSimplePayloadFilter());
        }
        return uriComponentsBuilder.toUriString();
    }

    private String getAbsoluteLinkForGetAnchorsDataBySchemaSet(final SearchCriteria searchCriteria,
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
        if (searchCriteria.getSimplePayloadFilter() != null) {
            uriComponentsBuilder.queryParam("simplePayloadFilter", searchCriteria.getSimplePayloadFilter());
        }
        return uriComponentsBuilder.toUriString();
    }
}
