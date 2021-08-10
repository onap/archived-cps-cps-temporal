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
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.service.NetworkDataService;
import org.onap.cps.temporal.utils.DateTimeUtility;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${rest.api.base-path}")
public class QueryController implements CpsTemporalQueryApi {

    private NetworkDataService networkDataService;

    public QueryController(final NetworkDataService networkDataService) {
        this.networkDataService = networkDataService;
    }

    @Override
    public ResponseEntity<AnchorHistory> getAnchorDataByName(final String dataspaceName,
        final String anchorName, final @Valid String observedTimestampAfter, final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Valid Integer pageLimit, final @Valid String sortAsString) {

        return process(dataspaceName,
            null, anchorName, observedTimestampAfter,
            simplePayloadFilter,
            pointInTime, pageNumber,
            pageLimit, sortAsString);
    }

    @Override
    public ResponseEntity<AnchorHistory> getAnchorsDataByFilter(final String dataspaceName,
        final @NotNull @Valid String schemaSetName, final @Valid String observedTimestampAfter,
        final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Valid Integer pageLimit, final @Valid String sortAsString) {
        return process(dataspaceName, schemaSetName, null,
            observedTimestampAfter,
            simplePayloadFilter,
            pointInTime, pageNumber,
            pageLimit, sortAsString);
    }

    private ResponseEntity<AnchorHistory> process(final String dataspaceName,
        final String schemaSetName, final String anchorName, final String observedTimestampAfter,
        final String simplePayloadFilter,
        final String pointInTime, final Integer pageNumber,
        final Integer pageLimit, final String sortAsString) {

        final var searchCriteriaBuilder = SearchCriteria.builder()
            .dataspaceName(dataspaceName)
            .schemaSetName(schemaSetName)
            .anchorName(anchorName)
            .pagination(pageNumber, pageLimit)
            .observedAfter(getOffsetDateTime(observedTimestampAfter, "observedTimestampAfter"))
            .simplePayloadFilter(simplePayloadFilter)
            .sort(getSort(sortAsString));

        if (!StringUtils.isEmpty(pointInTime)) {
            searchCriteriaBuilder.createdBefore(getOffsetDateTime(pointInTime, "pointInTime"));
        }

        final var searchCriteria = searchCriteriaBuilder.build();
        networkDataService.searchNetworkData(searchCriteria);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    private OffsetDateTime getOffsetDateTime(@NotEmpty final String datetime, final String propertyName) {
        try {
            return DateTimeUtility.getOffsetDateTime(datetime);
        } catch (final Exception exception) {
            throw new ValidationException(
                String.format("%s must be in \"yyyy-MM-dd'T'HH:mm:ss.SSSZ\" format", propertyName));
        }
    }

    /*
     * sortString format -> "<fieldname>:<direction>,...,<fieldname>:<direction>"
     * Example : "anchor:asc,observed_timestamp:desc"
     */
    private Sort getSort(@NotEmpty final String sortString) {
        try {
            final String[] sortingOrderAsString = sortString.split(",");
            final List<Order> sortOrder = new ArrayList<>();
            for (final String eachSortAsString : sortingOrderAsString) {
                final String[] eachSortDetail = eachSortAsString.split(":");
                final var direction = Direction.fromString(eachSortDetail[1]);
                final String fieldName = eachSortDetail[0];
                sortOrder.add(new Order(direction, fieldName));
            }
            return Sort.by(sortOrder);
        } catch (final Exception exception) {
            throw new ValidationException(
                "sort must be in '<fieldname>:<direction>,...,<fieldname>:<direction>' format. "
                    + "Example: 'anchor:asc,observed_timestamp:desc'", exception
            );
        }
    }
}
