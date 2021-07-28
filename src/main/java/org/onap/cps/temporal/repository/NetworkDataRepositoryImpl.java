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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class NetworkDataRepositoryImpl implements NetworkDataQueryRespository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<NetworkData> findBySearchCriteria(final SearchCriteria searchCriteria) {

        final var searchCriteriaQueryBuilder = new SearchCriteriaQueryBuilder(searchCriteria);
        searchCriteriaQueryBuilder.buildQuery();

        final long count = getTotalRowCount(searchCriteriaQueryBuilder.getCountNativeQuery(),
            searchCriteriaQueryBuilder.getQueryParameters());

        final List<NetworkData> data = getNetworkDataList(searchCriteriaQueryBuilder.getDataNativeQuery(),
            searchCriteriaQueryBuilder.getQueryParameters(), searchCriteria.getPageable());

        return new PageImpl<>(data, searchCriteria.getPageable(), count);
    }

    private long getTotalRowCount(final String nativeCountQuery, final Map<String, Object> queryParameters) {
        final Query countQuery = entityManager.createNativeQuery(nativeCountQuery);
        queryParameters.forEach(countQuery::setParameter);
        return ((BigInteger) countQuery.getSingleResult()).longValue();
    }

    private List<NetworkData> getNetworkDataList(final String nativeDataQuery,
        final Map<String, Object> queryParameters, final Pageable pageable) {
        final Query dataQuery = entityManager.createNativeQuery(nativeDataQuery, NetworkData.class);
        queryParameters.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        dataQuery.setMaxResults(pageable.getPageSize());
        return dataQuery.getResultList();
    }


    // TODO It is not an builder
    private static class SearchCriteriaQueryBuilder {

        @Getter
        private Map<String, Object> queryParameters = new HashMap<>();
        private StringBuilder queryBuilder = new StringBuilder();

        private String countQueryCondition;
        private String dataQueryCondition;

        private final SearchCriteria searchCriteria;

        SearchCriteriaQueryBuilder(final SearchCriteria searchCriteria) {
            this.searchCriteria = searchCriteria;
        }

        private void buildQuery() {

            queryBuilder.append(" WHERE dataspace = :dataspace ");
            queryParameters.put("dataspace", searchCriteria.getDataspaceName());

            addAnchorsCondition();
            addSchemaSetCondition();
            addObservedAfterCondition();
            addSimplePayloadCondition();

            addCreatedBeforeCondition();
            countQueryCondition = queryBuilder.toString();

            addOrderBy();
            dataQueryCondition = queryBuilder.toString();
            //addPagination();

        }


        private void addSchemaSetCondition() {
            if (!StringUtils.isEmpty(searchCriteria.getSchemaSetName())) {
                queryBuilder.append(" AND schema_set = :schemaSetName ");
                queryParameters.put("schemaSetName", searchCriteria.getSchemaSetName());
            }
        }

        private void addAnchorsCondition() {
            if (!searchCriteria.getAnchorNames().isEmpty()) {
                queryBuilder.append(" AND anchor in :anchorNames");
                queryParameters.put("anchorNames", searchCriteria.getAnchorNames());
            }
        }

        private void addSimplePayloadCondition() {
            if (!StringUtils.isEmpty(searchCriteria.getSimplePayloadFilter())) {
                queryBuilder.append(" AND payload @> :simplePayloadFilter\\:\\:jsonb ");
                queryParameters.put("simplePayloadFilter", searchCriteria.getSimplePayloadFilter());
            }
        }

        private void addCreatedBeforeCondition() {
            if (searchCriteria.getCreatedTimestampBefore() != null) {
                queryBuilder.append(" AND created_timestamp <= :createdBefore");
                queryParameters.put("createdBefore", searchCriteria.getCreatedTimestampBefore());
            }
        }

        private void addObservedAfterCondition() {
            if (searchCriteria.getObservedTimestampAfter() != null) {
                queryBuilder.append(" AND observed_timestamp >= :after");
                queryParameters.put("after", searchCriteria.getObservedTimestampAfter());
            }
        }

        private void addOrderBy() {
            final Sort sortBy = searchCriteria.getPageable().getSort();
            queryBuilder.append(" order by ");
            final String orderByQuery = sortBy.stream().map(order -> {
                final String direction = order.isAscending() ? "asc" : "desc";
                return order.getProperty() + " " + direction;
            }).collect(Collectors.joining(","));
            queryBuilder.append(orderByQuery);
        }

        String getDataNativeQuery() {
            return "select * from network_data nd " + dataQueryCondition;
        }

        String getCountNativeQuery() {
            return "select count(1) from network_data nd " + countQueryCondition;
        }

    }


}

