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

package org.onap.cps.temporal.controller.event.listener.kafka;

import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.MISSING;
import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.UNEXPECTED;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.event.model.CpsDataUpdatedEvent;
import org.onap.cps.temporal.controller.event.listener.exception.EventListenerException;
import org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException;
import org.onap.cps.temporal.controller.event.model.CpsDataUpdatedEventMapper;
import org.onap.cps.temporal.service.NetworkDataService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Listener for data updated events.
 */
@Component
@Slf4j
public class DataUpdatedEventListener {

    private static final URI EVENT_SOURCE;

    static {
        try {
            EVENT_SOURCE = new URI("urn:cps:org.onap.cps");
        } catch (final URISyntaxException e) {
            throw new EventListenerException(e);
        }
    }

    private static final String EVENT_TYPE = "org.onap.cps.data-updated-event";

    private final NetworkDataService networkDataService;
    private final CpsDataUpdatedEventMapper cpsDataUpdatedEventMapper;

    /**
     * Constructor.
     */
    public DataUpdatedEventListener(
            final NetworkDataService networkDataService, final CpsDataUpdatedEventMapper cpsDataUpdatedEventMapper) {
        this.networkDataService = networkDataService;
        this.cpsDataUpdatedEventMapper = cpsDataUpdatedEventMapper;
    }

    /**
     * Consume the specified event.
     *
     * @param cpsDataUpdatedEvent the data updated event to be consumed and persisted.
     */
    @KafkaListener(topics = "${app.listener.data-updated.topic}", errorHandler = "dataUpdatedEventListenerErrorHandler")
    public void consume(final CpsDataUpdatedEvent cpsDataUpdatedEvent) {

        log.debug("Receiving {} ...", cpsDataUpdatedEvent);

        // Validate event envelop
        validateEventEnvelop(cpsDataUpdatedEvent);

        // Map event to entity
        final var networkData = this.cpsDataUpdatedEventMapper.eventToEntity(cpsDataUpdatedEvent);
        log.debug("Persisting {} ...", networkData);

        // Persist entity
        final var persistedNetworkData = this.networkDataService.addNetworkData(networkData);
        log.debug("Persisted {}", persistedNetworkData);

    }

    private void validateEventEnvelop(final CpsDataUpdatedEvent cpsDataUpdatedEvent) {

        final var invalidEventEnvelopException = new InvalidEventEnvelopException();

        // Schema
        if (cpsDataUpdatedEvent.getSchema() == null) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            MISSING, "schema", null,
                            CpsDataUpdatedEvent.Schema.URN_CPS_ORG_ONAP_CPS_DATA_UPDATED_EVENT_SCHEMA_1_1_0_SNAPSHOT
                                    .value()));
        }
        // Id
        if (!StringUtils.hasText(cpsDataUpdatedEvent.getId())) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            MISSING, "id", null, null));
        }
        // Source
        if (cpsDataUpdatedEvent.getSource() == null || !cpsDataUpdatedEvent.getSource().equals(EVENT_SOURCE)) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "source",
                            cpsDataUpdatedEvent.getSource() != null
                                    ? cpsDataUpdatedEvent.getSource().toString() : null, EVENT_SOURCE.toString()));
        }
        // Type
        if (cpsDataUpdatedEvent.getType() == null || !cpsDataUpdatedEvent.getType().equals(EVENT_TYPE)) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "type", cpsDataUpdatedEvent.getType(), EVENT_TYPE));
        }

        if (invalidEventEnvelopException.hasInvalidFields()) {
            throw invalidEventEnvelopException;
        }

    }

}
