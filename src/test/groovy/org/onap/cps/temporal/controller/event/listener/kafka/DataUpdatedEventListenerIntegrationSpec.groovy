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

package org.onap.cps.temporal.controller.event.listener.kafka

import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.temporal.repository.containers.TimescaleContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

/**
 * Integration test specification for data updated event listener.
 * This integration test is running database and kafka dependencies as docker containers.
 */
@SpringBootTest
@Testcontainers
class DataUpdatedEventListenerIntegrationSpec extends Specification {

    @Shared
    TimescaleContainer timescaleTestContainer = TimescaleContainer.getInstance()

    static kafkaTestContainer = new KafkaContainer()
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec() {
        kafkaTestContainer.start()
    }

    @Autowired
    KafkaTemplate<String, CpsDataUpdatedEvent> kafkaTemplate

    @Autowired
    JdbcTemplate jdbcTemplate

    @Value('${app.listener.data-updated.topic}')
    String topic

    def 'Processing a valid event'() {
        def aTimestamp = EventFixtures.currentIsoTimestamp()
        given: 'no data exist for the anchor'
            assert networkDataConditionalCount(aTimestamp, 'my-dataspace', 'my-schema-set', 'my-anchor') == 0
        when: 'an event is produced'
            def event =
                    EventFixtures.buildEvent(
                            observedTimestamp: aTimestamp, dataspace: 'my-dataspace', schemaSet: 'my-schema-set',
                            anchor: 'my-anchor', data: ['my-data-name': 'my-data-value'])
            this.kafkaTemplate.send(topic, event)
        then: 'the event is processed and data exists in database'
            def pollingCondition = new PollingConditions(timeout: 10, initialDelay: 1, factor: 2)
            pollingCondition.eventually {
                assert networkDataConditionalCount(aTimestamp, 'my-dataspace', 'my-schema-set', 'my-anchor') == 1
            }
    }

    def 'Processing an invalid event'() {
        given: 'the number of network data records if known'
            def initialRecordsCount = networkDataAllRecordCount()
        when: 'an invalid event is produced'
            this.kafkaTemplate.send(topic, (CpsDataUpdatedEvent) null)
        then: 'the event is not proceeded and no more network data record is created'
            TimeUnit.SECONDS.sleep(3)
            networkDataAllRecordCount() == initialRecordsCount
    }

    def networkDataAllRecordCount() {
        return jdbcTemplate.queryForObject('SELECT COUNT(1) FROM network_data', Integer.class)
    }

    def networkDataConditionalCount(observedTimestamp, dataspaceName, schemaSetName, anchorName) {
        def recordCount = jdbcTemplate.queryForObject('SELECT COUNT(1) FROM network_data ' +
                'WHERE observed_timestamp = to_timestamp(?, \'YYYY-MM-DD"T"HH24:MI:SS.USTZHTZM\') ' +
                'AND dataspace = ? ' +
                'AND schema_set = ? ' +
                'AND anchor = ?',
                Integer.class, observedTimestamp, dataspaceName, schemaSetName, anchorName)
        return recordCount
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }

}
