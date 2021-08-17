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

package org.onap.cps.temporal.controller.rest

import org.onap.cps.temporal.controller.rest.config.WebSecurityConfig
import org.onap.cps.temporal.controller.rest.model.AnchorHistory
import org.onap.cps.temporal.controller.rest.model.SortMapper
import org.onap.cps.temporal.service.NetworkDataService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(QueryController)
@Import([WebSecurityConfig, SortMapper])
class ControllerSecuritySpec extends Specification {

    @SpringBean
    NetworkDataService mockNetworkDataService = Mock()

    @SpringBean
    QueryResponseFactory mockQueryResponseFactory = Mock()

    MockMvc mvc

    @Autowired
    WebApplicationContext context;

    @Shared
    def testEndpoint = '/cps-temporal/api/v1/dataspaces/my-dataspace/anchors/my-anchor/history'

    def setup() {
        mvc = MockMvcBuilders.webAppContextSetup(this.context).apply(springSecurity()).build();
    }

    def 'Get request with authentication: #scenario.'() {
        given: 'authentication'
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBasicAuth(username, password)
        and:
            mockQueryResponseFactory.createAnchorDataByNameResponse(_, _) >> new AnchorHistory()
        when: 'request is sent with authentication'
            def response = mvc.perform(get(testEndpoint).headers(httpHeaders)
            ).andReturn().response
        then: 'expected http status is returned'
            assert response.status == expectedHttpStatus.value()
        where:
            scenario              | username       | password         || expectedHttpStatus
            'correct credentials' | 'testUser'     | 'testPassword'   || HttpStatus.OK
            'unknown username'    | 'unknown-user' | 'password'       || HttpStatus.UNAUTHORIZED
            'wrong password'      | 'cpsuser'      | 'wrong-password' || HttpStatus.UNAUTHORIZED
    }

    def 'Get urls without authentication : #scenario.'() {
        when: 'request is sent without authentication'
            def response = mvc.perform(get(url)
            ).andReturn().response
        then: 'expected http status is returned'
            assert response.status == expectedHttpStatus.value()
        where:
            scenario            | url                    | expectedHttpStatus
            'permitted url'     | '/swagger/openapi.yml' | HttpStatus.OK
            'not-permitted url' | testEndpoint           | HttpStatus.UNAUTHORIZED
    }

}
