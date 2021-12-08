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

import org.mapstruct.factory.Mappers
import org.onap.cps.event.model.Content
import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException
import org.onap.cps.temporal.controller.event.model.CpsDataUpdatedEventMapper
import org.onap.cps.temporal.domain.Operation
import org.onap.cps.temporal.service.NetworkDataService
import spock.lang.Specification

import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.MISSING
import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.UNEXPECTED

/**
 * Test specification for data updated event listener.
 */
class DataUpdatedEventListenerSpec extends Specification {

    public static final String EXPECTED_SCHEMA_EXCEPTION_MESSAGE = 'urn:cps:org.onap.cps:data-updated-event-schema:v99'

    // Define event data
    def anEventType = 'my-event-type'
    def anEventSchema = new URI('my-event-schema')
    def anEventSource = new URI('my-event-source')
    def aTimestamp = EventFixtures.currentIsoTimestamp()
    def aDataspace = 'my-dataspace'
    def aSchemaSet = 'my-schema-set'
    def anAnchor = 'my-anchor'
    def aDataName = 'my-data-name'
    def aDataValue = 'my-data-value'

    // Define service mock
    def mockService = Mock(NetworkDataService)

    // Define mapper
    def mapper = Mappers.getMapper(CpsDataUpdatedEventMapper.class)

    // Define listener under test
    def objectUnderTest = new DataUpdatedEventListener(mockService, mapper)

    def 'Event message consumption'() {
        when: 'an event is received'
            def defaultEventProperties = [observedTimestamp: aTimestamp, dataspace: aDataspace,
                                          schemaSet        : aSchemaSet, anchor: anAnchor,
                                          dataName         : aDataName, dataValue: aDataValue]
            def addOperationField = eventSpecific != null ? [operation: Content.Operation.valueOf(eventSpecific)] : []
            def event =
                    EventFixtures.buildEvent(defaultEventProperties + addOperationField)
            objectUnderTest.consume(event)
        then: 'network data service is requested to persisted the data change'
            1 * mockService.addNetworkData(
                    {
                        it.getObservedTimestamp() == EventFixtures.toOffsetDateTime(aTimestamp)
                                && it.getDataspace() == aDataspace
                                && it.getSchemaSet() == aSchemaSet
                                && it.getAnchor() == anAnchor
                                && it.getCreatedTimestamp() == null
                                && it.getOperation() == expectedOperation
                                && it.getPayload() == String.format('{"%s":"%s"}', aDataName, aDataValue)

                    }
            )
        where:
            scenario                  | eventSpecific || expectedOperation
            'without operation field' | null          || Operation.UPDATE
            'create operation'        | "CREATE"      || Operation.CREATE
    }

    def 'Delete Event message consumption'() {
        when: 'an delete event is received'
            def deleteEvent =
                    EventFixtures.buildEvent([observedTimestamp: aTimestamp, dataspace: aDataspace,
                                              schemaSet        : aSchemaSet, anchor: anAnchor,
                                              operation        : Content.Operation.DELETE])
            objectUnderTest.consume(deleteEvent)
        then: 'network data service is requested to persisted the data change'
            1 * mockService.addNetworkData(
                    {
                        it.getObservedTimestamp() == EventFixtures.toOffsetDateTime(aTimestamp)
                                && it.getDataspace() == aDataspace
                                && it.getSchemaSet() == aSchemaSet
                                && it.getAnchor() == anAnchor
                                && it.getCreatedTimestamp() == null
                                && it.getOperation() == Operation.DELETE
                                && it.getPayload() == null
                    }
            )
    }

    def 'Event message consumption fails because of missing envelop'() {
        when: 'an event without envelop information is received'
            def invalidEvent = new CpsDataUpdatedEvent().withSchema(null)
            objectUnderTest.consume(invalidEvent)
        then: 'an exception is thrown with 4 invalid fields'
            def e = thrown(InvalidEventEnvelopException)
            e.getCpsDataUpdatedEvent() == invalidEvent
            e.getInvalidFields().size() == 4
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "schema", null, EXPECTED_SCHEMA_EXCEPTION_MESSAGE))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            MISSING, "id", null, null))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "source", null, EventFixtures.defaultEventSource.toString()))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "type", null, EventFixtures.defaultEventType))
            e.getMessage().contains(e.getInvalidFields().toString())
    }

    def 'Event message consumption fails because of invalid envelop'() {
        when: 'an event with an invalid envelop is received'
            def invalidEvent =
                    new CpsDataUpdatedEvent()
                            .withId('my-id')
                            .withSchema(anEventSchema)
                            .withSource(anEventSource)
                            .withType(anEventType)
            objectUnderTest.consume(invalidEvent)
        then: 'an exception is thrown with 2 invalid fields'
            def e = thrown(InvalidEventEnvelopException)
            e.getCpsDataUpdatedEvent() == invalidEvent
            e.getInvalidFields().size() == 3
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "schema", anEventSchema.toString(),
                            EXPECTED_SCHEMA_EXCEPTION_MESSAGE))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "type", anEventType, EventFixtures.defaultEventType))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "source", anEventSource.toString(),
                            EventFixtures.defaultEventSource.toString()))
    }

}
