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

package org.onap.cps.temporal.controller.event.listener.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.onap.cps.event.model.v0.CpsDataUpdatedEvent;

/**
 * Class representing an invalid event envelop exception.
 * It refers to the invalid event and details the invalid fields it has.
 */
@Getter
public class InvalidEventEnvelopException extends EventListenerException {

    private final CpsDataUpdatedEvent cpsDataUpdatedEvent;
    private final List<InvalidField> invalidFields = new ArrayList<>();

    public InvalidEventEnvelopException(final String message, final CpsDataUpdatedEvent cpsDataUpdatedEvent) {
        super(message);
        this.cpsDataUpdatedEvent = cpsDataUpdatedEvent;
    }

    public void addInvalidField(final InvalidField invalidField) {
        this.invalidFields.add(invalidField);
    }

    public boolean hasInvalidFields() {
        return ! this.invalidFields.isEmpty();
    }

    @Override
    public String getMessage() {
        return
                String.format("%s. Event: %s. Invalid fields: %s",
                        super.getMessage(), this.cpsDataUpdatedEvent, this.invalidFields.toString());
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class InvalidField implements Serializable {

        private static final long serialVersionUID = -7118283787669377391L;

        private final ErrorType errorType;
        private final String fieldName;
        private final String actualValue;
        private final String expectedValue;

        public enum ErrorType {
            UNEXPECTED, MISSING
        }

    }

}
