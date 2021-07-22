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

package org.onap.cps.temporal.controller.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import spock.lang.Specification

/**
 * Specification for Query Controller.
 */
@WebMvcTest(QueryController)
class QueryControllerSpec extends Specification {

    @Autowired
    MockMvc mvc

    @Value('${rest.api.base-path}')
    def basePath

    def myDataspace = 'my-dataspace'
    def myAnchor = 'my-anchor'
    def mySchemaset = 'my-schemaset'

    def 'Get anchors by name is not implemented.'(){
        given: 'an endpoint'
            def getAnchorsByNameEndpoint = "${basePath}/v1/dataspaces/{dataspace-name}/anchors/{anchor-name}/history"

        when: 'get anchors by name endpoint is called'
            def response = mvc.perform( get(getAnchorsByNameEndpoint, myDataspace, myAnchor)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response

        then: 'received unsupported operation response'
            response.getStatus() == HttpStatus.NOT_IMPLEMENTED.value()

    }

    def 'Get anchors by dataspace name is not implemented.'(){
        given: 'an endpoint'
            def getAnchorsByDataspaceEndpoint = "${basePath}/v1/dataspaces/{dataspace-name}/anchors/history"

        when: 'get anchors by dataspace name endpoint is called'
            def response = mvc.perform( get(getAnchorsByDataspaceEndpoint, myDataspace).queryParam('schemaset-name', mySchemaset)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response

        then: 'received unsupported operation response'
            response.getStatus() == HttpStatus.NOT_IMPLEMENTED.value()

    }

}
