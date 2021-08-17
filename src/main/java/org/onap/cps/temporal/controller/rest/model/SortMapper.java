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

package org.onap.cps.temporal.controller.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
public class SortMapper {

    private static final String SORT_ORDER_SEPARATOR = ",";
    private static final String FIELD_DIRECTION_SEPARATOR = ":";

    /**
     * convert from Sort to String format "fieldname:direction,...,fieldname:direction".
     *
     * @param sort sort
     * @return sort string
     */
    public String sortAsString(final Sort sort) {
        return sort.stream()
            .map(sortOrder ->
                sortOrder.getProperty() + FIELD_DIRECTION_SEPARATOR
                    + sortOrder.getDirection().toString().toLowerCase(Locale.ENGLISH))
            .collect(Collectors.joining(SORT_ORDER_SEPARATOR));
    }

    /**
     * Convert from "fieldname:direction,...,fieldname:direction" format to Sort. Example :
     * "anchor:asc,observed_timestamp:desc"
     *
     * @param sortString sortString
     * @return Sort
     */
    public Sort toSort(@NotEmpty final String sortString) {
        try {
            final String[] sortingOrderAsString = sortString.split(SORT_ORDER_SEPARATOR);
            final List<Order> sortOrder = new ArrayList<>();
            for (final String eachSortAsString : sortingOrderAsString) {
                final String[] eachSortDetail = eachSortAsString.split(FIELD_DIRECTION_SEPARATOR);
                final var direction = Direction.fromString(eachSortDetail[1]);
                final String fieldName = eachSortDetail[0];
                sortOrder.add(new Order(direction, fieldName));
            }
            return Sort.by(sortOrder);
        } catch (final Exception exception) {
            throw new ValidationException(
                String.format(
                    "Invalid sort format. sort '%s' is not in '<fieldname>:<direction>,...,<fieldname>:<direction>'"
                        + " format. Example: 'anchor:asc,observed_timestamp:desc'", sortString), exception
            );
        }
    }
}
