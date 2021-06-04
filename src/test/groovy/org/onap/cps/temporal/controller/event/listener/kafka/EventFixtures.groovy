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

package org.onap.cps.temporal.controller.event.listener.kafka

import org.onap.cps.event.model.Content
import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.event.model.Data

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * This class contains utility fixtures methods for building and manipulating event data.
 */
class EventFixtures {

    static DateTimeFormatter isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    static String defaultEventType = 'org.onap.cps.data-updated-event'
    static URI defaultEventSource = new URI('urn:cps:org.onap.cps')

    static CpsDataUpdatedEvent buildEvent(final Map map) {
        CpsDataUpdatedEvent event =
                new CpsDataUpdatedEvent()
                        .withId(
                                map.id != null ? map.id.toString() : UUID.randomUUID().toString())
                        .withType(
                                map.eventType != null ? map.eventType.toString() : defaultEventType)
                        .withSource(
                                map.eventSource != null ? new URI(map.eventSource.toString()) : defaultEventSource)
                        .withContent(
                                new Content()
                                        .withObservedTimestamp(
                                                map.timestamp != null ? map.timestamp.toString() : currentTimestamp())
                                        .withDataspaceName(
                                                map.dataspace != null ? map.dataspace.toString() : 'a-dataspace')
                                        .withSchemaSetName(
                                                map.schemaSet != null ? map.schemaSet.toString() : 'a-schema-set')
                                        .withAnchorName(
                                                map.anchor != null ? map.anchor.toString() : 'an-anchor')
                                        .withData(
                                                new Data().withAdditionalProperty(
                                                        map.dataName != null ? map.dataName.toString() : 'a-data-name',
                                                        map.dataValue != null ? map.dataValue : 'a-data-value')))

        return event
    }

    static String currentIsoTimestamp() {
        return isoTimestampFormatter.format(OffsetDateTime.now())
    }

    static OffsetDateTime toOffsetDateTime(String timestamp) {
        return OffsetDateTime.parse(timestamp, isoTimestampFormatter)
    }

}
