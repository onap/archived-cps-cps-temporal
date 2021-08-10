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

package org.onap.cps.temporal.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
@EqualsAndHashCode
public class SearchCriteria {

    private OffsetDateTime createdBefore;
    private OffsetDateTime observedAfter;
    private String dataspaceName;
    private String anchorName;
    private String schemaSetName;
    private Pageable pageable;
    private String simplePayloadFilter;

    public static class Builder {

        private static ObjectMapper objectMapper = new ObjectMapper();
        // TODO check if sorting by observed_timestamp must be there?
        private static final List<Order> SUPPORTED_SORT_ORDERS = List.of(Order.desc("observed_timestamp"),
            Order.asc("anchor"));

        private Sort sort = Sort.by(Direction.DESC, "observed_timestamp");
        private OffsetDateTime createdBefore = OffsetDateTime.now();

        public Builder pagination(final int pageNumber, final int pageSize) {
            pageable = PageRequest.of(pageNumber, pageSize);
            return this;
        }

        /**
         * Validate that simplePayloadFilter is a valid json.
         *
         * @param simplePayloadFilter simplePayloadFilter
         * @return Builder
         */
        public Builder simplePayloadFilter(final String simplePayloadFilter) {
            if (!StringUtils.isEmpty(simplePayloadFilter)) {
                try {
                    objectMapper.readValue(simplePayloadFilter, ObjectNode.class);
                    this.simplePayloadFilter = simplePayloadFilter;
                } catch (final JsonProcessingException jsonProcessingException) {
                    throw new IllegalArgumentException("simplePayloadFilter must be a valid json");
                }
            }
            return this;
        }

        /**
         * Validates the input with the expected list and saves only if matches.
         *
         * @param sort sort
         * @return Builder builder
         */
        public Builder sort(final Sort sort) {
            if (sort == null) {
                throw new IllegalArgumentException("sort must not be null");
            }
            if (!SUPPORTED_SORT_ORDERS.containsAll(sort.toList())) {
                throw new IllegalArgumentException(
                    "Invalid sorting. Supported sorts are " + SUPPORTED_SORT_ORDERS.toString());
            }
            this.sort = sort;
            return this;
        }

        /**
         * Validates the state before building search criteria.
         *
         * @return SearchCriteria searchCriteria
         */
        public SearchCriteria build() {

            if (StringUtils.isEmpty(anchorName) && StringUtils.isEmpty(schemaSetName)) {
                throw new IllegalStateException(
                    "Either anchorName or schemaSetName must be provided");
            }

            if (StringUtils.isEmpty(dataspaceName)) {
                throw new IllegalStateException("Dataspace is mandatory");
            }

            if (pageable == null) {
                throw new IllegalStateException("Pageable is mandatory");
            }

            final var searchCriteria = new SearchCriteria();
            searchCriteria.createdBefore = createdBefore;
            searchCriteria.observedAfter = observedAfter;
            searchCriteria.dataspaceName = dataspaceName;
            searchCriteria.anchorName = anchorName;
            searchCriteria.schemaSetName = schemaSetName;
            searchCriteria.pageable = ((PageRequest) pageable).withSort(sort);
            searchCriteria.simplePayloadFilter = simplePayloadFilter;
            return searchCriteria;
        }

    }

}


