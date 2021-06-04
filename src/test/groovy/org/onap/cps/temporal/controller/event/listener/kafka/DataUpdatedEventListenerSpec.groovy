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

import org.onap.cps.temporal.controller.event.model.CpsDataUpdatedEventMapper

import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.MISSING;
import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.UNEXPECTED;

import org.mapstruct.factory.Mappers;
import org.onap.cps.event.model.Content
import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.event.model.Data
import org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException
import org.onap.cps.temporal.service.NetworkDataService
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Test specification for data updated event listener.
 */
class DataUpdatedEventListenerSpec extends Specification {

    // Define event related constants

    def isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def eventSource = new URI('urn:cps:org.onap.cps')
    def eventType = 'org.onap.cps.data-updated-event'

    def anId = 'an-id'
    def aSource = new URI('a-source')
    def aType = 'a-type'
    def aTimestamp = '2021-05-26T00:00:00.123-0400'
    def aDataspace = 'a-dataspace'
    def aSchemaSet = 'a-schema-set'
    def anAnchor = 'an-anchor'
    def aDataName = 'a-data-name'
    def aDataValue = 'a-data-value'

    def event =
            new CpsDataUpdatedEvent()
                    .withId(anId)
                    .withSource(eventSource)
                    .withType(eventType)
                    .withContent(
                            new Content()
                                    .withObservedTimestamp(aTimestamp)
                                    .withDataspaceName(aDataspace)
                                    .withSchemaSetName(aSchemaSet)
                                    .withAnchorName(anAnchor)
                                    .withData(new Data().withAdditionalProperty(aDataName, aDataValue)))

    // Define service mock
    def mockService = Mock(NetworkDataService)

    // Define mapper
    def mapper = Mappers.getMapper(CpsDataUpdatedEventMapper.class)

    // Define listener under test
    def objectUnderTest = new DataUpdatedEventListener(mockService, mapper)

    def 'Event message consumption'() {
        when: 'an event is received'
            objectUnderTest.consume(event)
        then: 'network data service is requested to persisted the data change'
            1 * mockService.addNetworkData(
                    {
                        it.getObservedTimestamp() == OffsetDateTime.parse(aTimestamp, isoTimestampFormatter)
                        && it.getDataspace() == aDataspace
                        && it.getSchemaSet() == aSchemaSet
                        && it.getAnchor() == anAnchor
                        && it.getPayload() == String.format('{"%s":"%s"}', aDataName, aDataValue)
                        && it.getCreatedTimestamp() == null
                    }
            )
    }

    def 'Event message consumption fails because of missing envelop'() {
        when: 'an event without envelop information is received'
            def invalidEvent = new CpsDataUpdatedEvent().withSchema(null)
            objectUnderTest.consume(invalidEvent)
        then: 'an exception is thrown'
            def e = thrown(InvalidEventEnvelopException)
            e.getInvalidFields().size() == 4
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(
                            MISSING,"schema", null,
                            CpsDataUpdatedEvent.Schema.URN_CPS_ORG_ONAP_CPS_DATA_UPDATED_EVENT_SCHEMA_1_1_0_SNAPSHOT
                                    .value()))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(MISSING, "id", null, null))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(UNEXPECTED, "source", null, eventSource.toString()))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(UNEXPECTED, "type", null, eventType))
    }

    def 'Event message consumption fails because of invalid envelop'() {
        when: 'an event with an invalid envelop is received'
            def invalidEvent = new CpsDataUpdatedEvent().withId(anId).withSource(aSource).withType(aType)
            objectUnderTest.consume(invalidEvent)
        then: 'an exception is thrown'
            def e = thrown(InvalidEventEnvelopException)
            e.getInvalidFields().size() == 2
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(UNEXPECTED, "type", aType, eventType))
            e.getInvalidFields().contains(
                    new InvalidEventEnvelopException.InvalidField(UNEXPECTED, "source", aSource.toString(), eventSource.toString()))
    }

}
