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

import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.onap.cps.temporal.controller.rest.model.SortMapper;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.service.NetworkDataService;
import org.onap.cps.temporal.utils.DateTimeUtility;
import org.springframework.data.domain.Slice;
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
     * @param networkDataService   networkDataService
     * @param sortMapper           sortMapper
     * @param queryResponseFactory anchorHistoryResponseFactory
     */
    public QueryController(final NetworkDataService networkDataService,
        final SortMapper sortMapper,
        final QueryResponseFactory queryResponseFactory) {
        this.networkDataService = networkDataService;
        this.sortMapper = sortMapper;
        this.queryResponseFactory = queryResponseFactory;
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

    private OffsetDateTime getOffsetDateTime(@NotEmpty final String datetime, final String propertyName) {
        try {
            return DateTimeUtility.getOffsetDateTime(datetime);
        } catch (final Exception exception) {
            throw new ValidationException(
                String.format("%s must be in '%s' format", propertyName, DateTimeUtility.ISO_TIMESTAMP_PATTERN));
        }
    }


}
