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
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.temporal.controller.rest.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = QueryController.class)
public class QueryExceptionHandler {

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorMessage> handleClientError(final ValidationException validationException) {
        return buildErrorMessage(HttpStatus.BAD_REQUEST, validationException);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorMessage> handleClientError(final IllegalArgumentException illegalArgumentException) {
        return logAndBuildErrorMessage(HttpStatus.BAD_REQUEST, illegalArgumentException);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessage> handleInternalServerError(final Exception exception) {
        return logAndBuildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<ErrorMessage> logAndBuildErrorMessage(final HttpStatus httpStatus,
        final Exception exception) {
        logException(exception);
        return buildErrorMessage(httpStatus, exception);
    }

    private void logException(final Exception exception) {
        final String message = String.format("Failed to process : %s. Error cause is %s",
            exception.getMessage(),
            exception.getCause() != null ? exception.getCause().toString() : null);
        log.error(message, exception);

    }

    private ResponseEntity<ErrorMessage> buildErrorMessage(final HttpStatus httpStatus,
        final Exception exception) {
        final var errorMessage = new ErrorMessage();
        errorMessage.setStatus(Integer.toString(httpStatus.value()));
        errorMessage.setMessage(exception.getMessage());
        return ResponseEntity.status(httpStatus).body(errorMessage);
    }


}
