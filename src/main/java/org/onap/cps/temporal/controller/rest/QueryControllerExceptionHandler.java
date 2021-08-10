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

import javax.validation.ValidationException;
import org.onap.cps.temporal.controller.rest.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = QueryController.class)
public class QueryControllerExceptionHandler {

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorMessage> handleClientError(final ValidationException validationException) {
        return buildErrorMessage(HttpStatus.BAD_REQUEST, validationException.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorMessage> handleClientError(final IllegalArgumentException illegalArgumentException) {
        return buildErrorMessage(HttpStatus.BAD_REQUEST, illegalArgumentException.getMessage());
    }

    private ResponseEntity<ErrorMessage> buildErrorMessage(final HttpStatus httpStatus, final String message) {
        final var errorMessage = new ErrorMessage();
        errorMessage.setStatus(Integer.toString(httpStatus.value()));
        errorMessage.setMessage(message);
        return ResponseEntity.status(httpStatus).body(errorMessage);
    }


}
