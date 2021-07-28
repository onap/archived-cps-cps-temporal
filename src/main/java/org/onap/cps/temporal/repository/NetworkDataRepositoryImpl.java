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

package org.onap.cps.temporal.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class NetworkDataRepositoryImpl implements NetworkDataQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /*
    Slice is the response type instead of List<NetworkData> to provide the information if next set of data is available.
    To identify if next slice is available, the getDatNetworkDataList fetches one record extra ( n+1).
    If ( n +1) records are fetched, it means that the next slice exist, otherwise it does not.
     */
    @Override
    public Slice<NetworkData> findBySearchCriteria(final SearchCriteria searchCriteria) {

        final var searchCriteriaQueryBuilder = new SearchCriteriaQueryBuilder(searchCriteria);
        searchCriteriaQueryBuilder.buildQuery();

        final List<NetworkData> data = getNetworkDataList(searchCriteriaQueryBuilder.getDataNativeQuery(),
            searchCriteriaQueryBuilder.getQueryParameters(), searchCriteria.getPageable());

        final boolean hasNextSlice = data.size() > searchCriteria.getPageable().getPageSize();
        final List<NetworkData> sliceData = new ArrayList<>(data);
        if (hasNextSlice) {
            sliceData.remove(searchCriteria.getPageable().getPageSize());
        }

        return new SliceImpl<>(sliceData, searchCriteria.getPageable(), hasNextSlice);
    }

    private List<NetworkData> getNetworkDataList(final String nativeDataQuery,
        final Map<String, Object> queryParameters, final Pageable pageable) {
        final var dataQuery = entityManager.createNativeQuery(nativeDataQuery, NetworkData.class);
        queryParameters.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        dataQuery.setMaxResults(pageable.getPageSize() + 1);
        return dataQuery.getResultList();
    }

    private static class SearchCriteriaQueryBuilder {

        @Getter
        private Map<String, Object> queryParameters = new HashMap<>();
        private StringBuilder queryBuilder = new StringBuilder();

        private String dataQuery;

        private final SearchCriteria searchCriteria;

        SearchCriteriaQueryBuilder(final SearchCriteria searchCriteria) {
            this.searchCriteria = searchCriteria;
        }

        private void buildQuery() {

            queryBuilder.append("SELECT * FROM network_data nd WHERE dataspace = :dataspace ");
            queryParameters.put("dataspace", searchCriteria.getDataspaceName());

            addAnchorCondition();
            addSchemaSetCondition();
            addObservedAfterCondition();
            addSimplePayloadCondition();
            addCreatedBeforeCondition();
            addOrderBy();
            dataQuery = queryBuilder.toString();

        }


        private void addSchemaSetCondition() {
            if (!StringUtils.isEmpty(searchCriteria.getSchemaSetName())) {
                queryBuilder.append(" AND schema_set = :schemaSetName ");
                queryParameters.put("schemaSetName", searchCriteria.getSchemaSetName());
            }
        }

        private void addAnchorCondition() {
            if (!StringUtils.isEmpty(searchCriteria.getAnchorName())) {
                queryBuilder.append(" AND anchor = :anchorName");
                queryParameters.put("anchorName", searchCriteria.getAnchorName());
            }
        }

        private void addSimplePayloadCondition() {
            if (!StringUtils.isEmpty(searchCriteria.getSimplePayloadFilter())) {
                queryBuilder.append(" AND payload @> :simplePayloadFilter\\:\\:jsonb ");
                queryParameters.put("simplePayloadFilter", searchCriteria.getSimplePayloadFilter());
            }
        }

        private void addCreatedBeforeCondition() {
            if (searchCriteria.getCreatedBefore() != null) {
                queryBuilder.append(" AND created_timestamp <= :createdBefore");
                queryParameters.put("createdBefore", searchCriteria.getCreatedBefore());
            }
        }

        private void addObservedAfterCondition() {
            if (searchCriteria.getObservedAfter() != null) {
                queryBuilder.append(" AND observed_timestamp >= :observedAfter");
                queryParameters.put("observedAfter", searchCriteria.getObservedAfter());
            }
        }

        private void addOrderBy() {
            final var sortBy = searchCriteria.getPageable().getSort();
            queryBuilder.append(" ORDER BY ");
            final String orderByQuery = sortBy.stream().map(order -> {
                final var direction = order.isAscending() ? "asc" : "desc";
                return order.getProperty() + " " + direction;
            }).collect(Collectors.joining(","));
            queryBuilder.append(orderByQuery);
        }

        String getDataNativeQuery() {
            return dataQuery;
        }

    }


}

