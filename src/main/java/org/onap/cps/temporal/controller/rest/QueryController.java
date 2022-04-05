/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021-2022 Bell Canada
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.cps.temporal.controller.rest.model.AnchorDetails;
import org.onap.cps.temporal.controller.rest.model.AnchorDetailsMapper;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.onap.cps.temporal.controller.rest.model.SortMapper;
import org.onap.cps.temporal.controller.utils.DateTimeUtility;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.service.NetworkDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${rest.api.base-path}")
public class QueryController implements CpsTemporalQueryApi {

    private NetworkDataService networkDataService;
    private SortMapper sortMapper;
    private QueryResponseFactory queryResponseFactory;

    /**
     * Constructor.
     *
     * @param networkDataService  networkDataService
     * @param sortMapper          sortMapper
     * @param anchorDetailsMapper anchorDetailsMapper
     * @param basePath            basePath
     */
    public QueryController(final NetworkDataService networkDataService,
        final SortMapper sortMapper,
        final AnchorDetailsMapper anchorDetailsMapper,
        @Value("${rest.api.base-path}") final String basePath) {
        this.networkDataService = networkDataService;
        this.sortMapper = sortMapper;
        this.queryResponseFactory = new QueryResponseFactory(sortMapper, anchorDetailsMapper, basePath);
    }

    @Override
    public ResponseEntity<AnchorHistory> getAnchorDataByName(final String dataspaceName,
        final String anchorName, final @Valid String observedTimestampAfter,
        final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Valid Integer pageLimit, final @Valid String sortAsString) {

        final var searchCriteriaBuilder =
            getSearchCriteriaBuilder(observedTimestampAfter, simplePayloadFilter, pointInTime,
                pageNumber, pageLimit, sortAsString)
                .dataspaceName(dataspaceName).anchorName(anchorName);
        final var searchCriteria = searchCriteriaBuilder.build();
        final Slice<NetworkData> searchResult = networkDataService.searchNetworkData(searchCriteria);
        final var anchorHistory = queryResponseFactory
            .createAnchorDataByNameResponse(searchCriteria, searchResult);
        return ResponseEntity.ok(anchorHistory);
    }

    @Override
    public ResponseEntity<AnchorHistory> getAnchorsDataByFilter(final String dataspaceName,
        final @NotNull @Valid String schemaSetName, final @Valid String observedTimestampAfter,
        final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Valid Integer pageLimit, final @Valid String sortAsString) {
        final var searchCriteriaBuilder =
            getSearchCriteriaBuilder(observedTimestampAfter,
                simplePayloadFilter,
                pointInTime, pageNumber,
                pageLimit, sortAsString)
                .dataspaceName(dataspaceName).schemaSetName(schemaSetName);
        final var searchCriteria = searchCriteriaBuilder.build();
        final Slice<NetworkData> searchResult = networkDataService.searchNetworkData(searchCriteria);
        final var anchorHistory = queryResponseFactory
            .createAnchorsDataByFilterResponse(searchCriteria, searchResult);
        return ResponseEntity.ok(anchorHistory);
    }

    private SearchCriteria.Builder getSearchCriteriaBuilder(final String observedTimestampAfter,
        final String simplePayloadFilter,
        final String pointInTime, final Integer pageNumber,
        final Integer pageLimit, final String sortAsString) {

        final var searchCriteriaBuilder = SearchCriteria.builder()
            .pagination(pageNumber, pageLimit)
            .observedAfter(getOffsetDateTime(observedTimestampAfter, "observedTimestampAfter"))
            .simplePayloadFilter(simplePayloadFilter)
            .sort(sortMapper.toSort(sortAsString));

        if (!StringUtils.isEmpty(pointInTime)) {
            searchCriteriaBuilder.createdBefore(getOffsetDateTime(pointInTime, "pointInTime"));
        }

        return searchCriteriaBuilder;

    }

    private OffsetDateTime getOffsetDateTime(final String datetime, final String propertyName) {
        try {
            return DateTimeUtility.toOffsetDateTime(datetime);
        } catch (final Exception exception) {
            throw new ValidationException(
                String.format("%s must be in '%s' format", propertyName, DateTimeUtility.ISO_TIMESTAMP_PATTERN));
        }
    }


    public static class QueryResponseFactory {

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
            final String basePath) {
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
                        getRelativeLinkForGetAnchorsDataByFilter(searchCriteria, searchResult.nextPageable()));
            }
            if (searchResult.hasPrevious()) {
                anchorHistory.setPreviousRecordsLink(
                        getRelativeLinkForGetAnchorsDataByFilter(searchCriteria, searchResult.previousPageable()));
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
                anchorHistory.setNextRecordsLink(
                    getRelativeLinkForGetAnchorDataByName(searchCriteria, searchResult.nextPageable()));
            }
            if (searchResult.hasPrevious()) {
                anchorHistory.setPreviousRecordsLink(
                    getRelativeLinkForGetAnchorDataByName(searchCriteria, searchResult.previousPageable()));
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
            final int contextPathBeginIndex = absoluteLink.indexOf("${rest.api.base-path}");
            return basePath + absoluteLink.substring(contextPathBeginIndex + 21);
        }

        private String getRelativeLinkForGetAnchorDataByName(final SearchCriteria searchCriteria,
                                                             final Pageable pageable) {
            final Link absoluteLink = linkTo(methodOn(QueryController.class).getAnchorDataByName(
                searchCriteria.getDataspaceName(),
                searchCriteria.getAnchorName(),
                DateTimeUtility.toString(searchCriteria.getObservedAfter()),
                searchCriteria.getSimplePayloadFilter(),
                DateTimeUtility.toString(searchCriteria.getCreatedBefore()),
                pageable.getPageNumber(), pageable.getPageSize(),
                sortMapper.sortAsString(searchCriteria.getPageable().getSort()))).withSelfRel();
            return Link.of(toRelativeLink(absoluteLink.getHref())).expand().getHref();
        }

        private String getRelativeLinkForGetAnchorsDataByFilter(final SearchCriteria searchCriteria,
                                                                final Pageable pageable) {
            final Link absoluteLink = linkTo(methodOn(QueryController.class).getAnchorsDataByFilter(
                searchCriteria.getDataspaceName(),
                searchCriteria.getSchemaSetName(),
                DateTimeUtility.toString(searchCriteria.getObservedAfter()),
                searchCriteria.getSimplePayloadFilter(),
                DateTimeUtility.toString(searchCriteria.getCreatedBefore()),
                pageable.getPageNumber(), pageable.getPageSize(),
                sortMapper.sortAsString(searchCriteria.getPageable().getSort()))).withSelfRel();
            return Link.of(toRelativeLink(absoluteLink.getHref())).expand().getHref();
        }
    }
}
