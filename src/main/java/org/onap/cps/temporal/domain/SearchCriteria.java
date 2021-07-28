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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.CollectionUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Builder(builderClassName = "Builder")
public class SearchCriteria {

    private OffsetDateTime pointInTime;
    private OffsetDateTime after;
    private String dataspaceName;
    private Set<String> anchorNames = new HashSet<>();
    private String schemaSetName;
    private Pageable pageable;
    private String simplePayloadFilter;

    public static class Builder {

        private Sort sort = Sort.by(Direction.DESC, "observed_timestamp");
        private OffsetDateTime pointInTime = OffsetDateTime.now();

        public Builder pagination(final int pageNumber, final int pageSize) {
            pageable = PageRequest.of(pageNumber, pageSize);
            return this;
        }

        public Builder sort(final @NotNull Sort sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Validates the state before building search criteria.
         *
         * @return SearchCriteria searchCriteria
         */
        public SearchCriteria build() {

            if (CollectionUtils.isEmpty(anchorNames) && StringUtils.isEmpty(schemaSetName)) {
                throw new IllegalStateException(
                    "Either anchorNames or schemaSetName is mandatory");
            }

            if (StringUtils.isEmpty(dataspaceName)) {
                throw new IllegalStateException("Dataspace is mandatory");
            }

            if (pageable == null) {
                throw new IllegalStateException("Pageable is mandatory");
            }

            final var searchCriteria = new SearchCriteria();
            searchCriteria.pointInTime = pointInTime;
            searchCriteria.after = after;
            searchCriteria.dataspaceName = dataspaceName;
            searchCriteria.anchorNames = anchorNames;
            searchCriteria.schemaSetName = schemaSetName;
            searchCriteria.pageable = ((PageRequest) pageable).withSort(sort);
            searchCriteria.simplePayloadFilter = simplePayloadFilter;
            return searchCriteria;
        }

    }

}


