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

import groovy.util.logging.Slf4j
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
@Slf4j
class DataUpdatedEventListenerIntegrationSpec extends Specification {

    @Shared
    TimescaleContainer databaseTestContainer = TimescaleContainer.getInstance()

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

    // Define event data
    def aTimestamp = EventFixtures.currentIsoTimestamp()
    def aDataspace = 'my-dataspace'
    def aSchemaSet = 'my-schema-set'
    def anAnchor = 'my-anchor'
    def aDataName = 'my-data-name'
    def aDataValue = 'my-data-value'

    // Define sql queries for data validation
    def sqlCount = "select count(*) from network_data"
    def sqlSelect =  "select * from network_data"
    def sqlWhereClause =
            ' where observed_timestamp = to_timestamp(?, \'YYYY-MM-DD"T"HH24:MI:SS.USTZHTZM\') ' +
                    'and dataspace = ? ' +
                    'and schema_set = ? ' +
                    'and anchor = ?'
    def sqlCountWithConditions = sqlCount + sqlWhereClause
    def sqlSelectWithConditions = sqlSelect + sqlWhereClause

    def 'Processing a valid event'() {
        given: "no event has been proceeded"
            def initialRecordsCount =
                    jdbcTemplate.queryForObject(sqlCountWithConditions, Integer.class,
                            aTimestamp, aDataspace, aSchemaSet, anAnchor)
            assert (initialRecordsCount == 0)
        when: 'an event is produced'
            def event =
                    EventFixtures.buildEvent(
                            observedTimestamp: aTimestamp, dataspace: aDataspace, schemaSet: aSchemaSet,
                            anchor: anAnchor, dataName: aDataName, dataValue: aDataValue)
            this.kafkaTemplate.send(topic, event)
        then: 'the event is proceeded'
            def pollingCondition = new PollingConditions(timeout: 10, initialDelay: 1, factor: 2)
            pollingCondition.eventually {
                def finalRecordsCount =
                        jdbcTemplate.queryForObject(
                                sqlCountWithConditions, Integer.class, aTimestamp, aDataspace, aSchemaSet, anAnchor)
                assert (finalRecordsCount == 1)
            }
            Map<String, Object> result =
                    jdbcTemplate.queryForMap(sqlSelectWithConditions, aTimestamp, aDataspace, aSchemaSet, anAnchor)
            log.debug("Data retrieved from db: {}", result)
    }

    def 'Processing an invalid event'() {
        given: 'the number of network data records if known'
            def initialRecordsCount = jdbcTemplate.queryForObject(sqlCount, Integer.class)
        when: 'an invalid event is produced'
            this.kafkaTemplate.send(topic, (CpsDataUpdatedEvent) null)
        then: 'the event is not proceeded and no more network data record is created'
            TimeUnit.SECONDS.sleep(3)
            assert (jdbcTemplate.queryForObject(sqlCount, Integer.class) == initialRecordsCount)
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaTestContainer::getBootstrapServers)
    }

}
