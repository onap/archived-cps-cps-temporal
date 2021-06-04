/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.controller.event.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.onap.cps.event.model.CpsDataUpdatedEvent;
import org.onap.cps.event.model.Data;
import org.onap.cps.temporal.domain.NetworkData;

/**
 * Mapper for data updated event schema.
 */
@Mapper
public abstract class CpsDataUpdatedEventMapper {

    private static final DateTimeFormatter ISO_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Mapping(source = "content.observedTimestamp", target = "observedTimestamp")
    @Mapping(source = "content.dataspaceName", target = "dataspace")
    @Mapping(source = "content.schemaSetName", target = "schemaSet")
    @Mapping(source = "content.anchorName", target = "anchor")
    @Mapping(source = "content.data", target = "payload")
    @Mapping(target = "createdTimestamp", expression = "java(null)")
    public abstract NetworkData eventToEntity(CpsDataUpdatedEvent eventContent);

    String map(final Data data) throws JsonProcessingException {
        return data != null ? new ObjectMapper().writeValueAsString(data) : null;
    }

    OffsetDateTime map(final String timestamp) {
        return timestamp != null ? OffsetDateTime.parse(timestamp, ISO_TIMESTAMP_FORMATTER) : null;
    }

}
