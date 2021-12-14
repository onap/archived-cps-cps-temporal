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

package org.onap.cps.temporal.controller.event.model

import com.fasterxml.jackson.core.JsonProcessingException
import org.mapstruct.factory.Mappers
import org.onap.cps.event.model.Content
import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.event.model.Data
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.domain.Operation
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Test specification for data updated event mapper.
 */
class CpsDataUpdatedEventMapperSpec extends Specification {

    def objectUnderTest = Mappers.getMapper(CpsDataUpdatedEventMapper.class);

    def 'Mapping a null event'() {
        given: 'a null event'
            def event = null
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'the result entity is null'
            result == null
    }

    def 'Mapping an event whose properties are null'() {
        given: 'an event whose properties are null'
            def event = new CpsDataUpdatedEvent()
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'the result entity is not null'
            result != null
        and: 'all result entity properties are null'
            assertEntityPropertiesAreNull(result)
    }

    def 'Mapping an event whose content properties are null'() {
        given: 'an event whose content properties are null'
            def event = new CpsDataUpdatedEvent().withContent(new Content())
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'the result entity is not null'
            result != null
        and: 'all result entity properties are null'
            assertEntityPropertiesAreNull(result)
    }

    def 'Mapping an event whose content data is empty'() {
        given: 'an event whose content data is empty'
            def event = new CpsDataUpdatedEvent().withContent(new Content().withData(new Data()))
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'the result entity is not null'
            result != null
        and: 'the result entity payload is an empty json '
            result.getPayload() == '{}'
    }

    def 'Mapping an event whose content data is invalid'() {
        given: 'an event whose content data is invalid'
            def event =
                    new CpsDataUpdatedEvent().withContent(new Content().withData(
                            new Data().withAdditionalProperty(null, null)))
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'an runtime exception is thrown'
            def e = thrown(RuntimeException)
            e.getCause() instanceof JsonProcessingException
    }

    def 'Mapping a valid complete event'() {
        given: 'a valid complete event'
            def isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            def aDataName = 'a-data-name'
            def aDataValue = 'a-data-value'
            def event =
                    new CpsDataUpdatedEvent()
                            .withContent(
                                    new Content()
                                            .withObservedTimestamp(isoTimestampFormatter.format(OffsetDateTime.now()))
                                            .withDataspaceName('a-dataspace')
                                            .withSchemaSetName('a-schema-set')
                                            .withAnchorName('an-anchor')
                                            .withOperation(Content.Operation.CREATE)
                                            .withData(new Data().withAdditionalProperty(aDataName, aDataValue)))
        when: 'the event is mapped to an entity'
            NetworkData result = objectUnderTest.eventToEntity(event)
        then: 'the result entity is not null'
            result != null
        and: 'all result entity properties are the ones from the event'
            with(result) {
                observedTimestamp ==
                    OffsetDateTime.parse(event.getContent().getObservedTimestamp(), isoTimestampFormatter)
                dataspace == event.getContent().getDataspaceName()
                schemaSet == event.getContent().getSchemaSetName()
                operation == Operation.CREATE
                anchor == event.getContent().getAnchorName()
                createdTimestamp == null
            }
            result.getPayload().contains(aDataValue)
            result.getPayload().contains(aDataValue)
    }

    def 'Mapping event without operation field' () {
        given: 'event without operation field in content'
            def cpsDataUpdatedEvent = new CpsDataUpdatedEvent().withContent(new Content())
        when: 'event is mapped to network data'
            def networkData = objectUnderTest.eventToEntity(cpsDataUpdatedEvent)
        then: 'the operation field has default UPDATE value'
            networkData.operation == Operation.UPDATE
    }

    private void assertEntityPropertiesAreNull(NetworkData networkData) {
        assert networkData.getObservedTimestamp() == null
        assert networkData.getDataspace() == null
        assert networkData.getSchemaSet() == null
        assert networkData.getAnchor() == null
        assert networkData.getPayload() == null
        assert networkData.getCreatedTimestamp() == null
    }

}
