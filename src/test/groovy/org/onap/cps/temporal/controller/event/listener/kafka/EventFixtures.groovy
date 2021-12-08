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
    static URI defaultEventSchema = new URI('urn:cps:org.onap.cps:data-updated-event-schema:v2')
    static URI defaultEventSource = new URI('urn:cps:org.onap.cps')

    static def defaultEventValue = [
            eventSchema      : defaultEventSchema,
            id               : UUID.randomUUID().toString(),
            eventType        : defaultEventType,
            eventSource      : defaultEventSource,
            observedTimestamp: currentIsoTimestamp(),
            dataspace        : 'a-dataspace',
            schemaSet        : 'a-schema-set',
            anchor           : 'an-anchor'
    ]

    static CpsDataUpdatedEvent buildEvent(final Map inputMap) {
        def mergedMap = defaultEventValue + inputMap

        def dataExist = mergedMap.containsKey("dataName") && mergedMap.containsKey("dataValue")
        def data = dataExist ? new Data().withAdditionalProperty(
                mergedMap.dataName, mergedMap.dataValue) : null

        def content = new Content()
                .withObservedTimestamp(mergedMap.observedTimestamp)
                .withDataspaceName(mergedMap.dataspace)
                .withSchemaSetName(mergedMap.schemaSet)
                .withAnchorName(mergedMap.anchor)
                .withOperation(mergedMap.operation)
                .withData(data)

        CpsDataUpdatedEvent event =
                new CpsDataUpdatedEvent()
                        .withSchema(mergedMap.eventSchema)
                        .withId(mergedMap.id)
                        .withType(mergedMap.eventType)
                        .withSource(mergedMap.eventSource)
                        .withContent(content)

        return event
    }

    static String currentIsoTimestamp() {
        return isoTimestampFormatter.format(OffsetDateTime.now())
    }

    static OffsetDateTime toOffsetDateTime(String timestamp) {
        return OffsetDateTime.parse(timestamp, isoTimestampFormatter)
    }

}
