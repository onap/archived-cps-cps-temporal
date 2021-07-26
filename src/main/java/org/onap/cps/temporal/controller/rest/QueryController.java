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

package org.onap.cps.temporal.controller.rest;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.onap.cps.temporal.controller.rest.model.AnchorHistory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${rest.api.base-path}")
public class QueryController implements CpsTemporalQueryApi {

    @Override
    public ResponseEntity<AnchorHistory> getAnchorDataByName(final String dataspaceName,
        final String anchorName, final @Valid String after, final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Max(10000) @Valid Integer pageLimit, final @Valid String sort) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AnchorHistory> getAnchorsDataByFilter(final String dataspaceName,
        final @NotNull @Valid String schemaSetName, final @Valid String after, final @Valid String simplePayloadFilter,
        final @Valid String pointInTime, final @Min(0) @Valid Integer pageNumber,
        final @Min(0) @Max(10000) @Valid Integer pageLimit, final @Valid String sort) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
