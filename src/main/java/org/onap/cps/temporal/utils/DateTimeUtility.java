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

package org.onap.cps.temporal.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public interface DateTimeUtility {

    String ISO_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    DateTimeFormatter ISO_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(ISO_TIMESTAMP_PATTERN);

    static OffsetDateTime getOffsetDateTime(String datetTimestampAsString) {
        return StringUtils.isEmpty(datetTimestampAsString)
            ? null : OffsetDateTime.parse(datetTimestampAsString, ISO_TIMESTAMP_FORMATTER);
    }

    static String offsetDateTimeAsString(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? ISO_TIMESTAMP_FORMATTER.format(offsetDateTime) : null;
    }
}
