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

package org.onap.cps.temporal.controller.event.listener.kafka

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.cps.event.model.Content
import org.onap.cps.event.model.CpsDataUpdatedEvent
import org.onap.cps.event.model.Data
import org.onap.cps.temporal.repository.containers.TimescaleContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.testcontainers.containers.KafkaContainer
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Integration test specification for data updated event listener.
 * This integration test is running database and kafka dependencies as docker containers.
 */
@SpringBootTest
@Slf4j
class DataUpdatedEventListenerIntegrationSpec extends Specification {

    @Shared
    def databaseTestContainer = TimescaleContainer.getInstance()

    static kafkaTestContainer = new KafkaContainer()
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec() {
        databaseTestContainer.start()
        kafkaTestContainer.start()
    }

    @Autowired
    KafkaTemplate<String, CpsDataUpdatedEvent> kafkaTemplate

    @Autowired
    JdbcTemplate jdbcTemplate

    @Value('${app.listener.data-updated.topic}')
    String topic

    def eventSource = new URI('urn:cps:org.onap.cps')
    def eventType = 'org.onap.cps.data-updated-event'

    def isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def timestamp = OffsetDateTime.now()
    def timestampStr = isoTimestampFormatter.format(timestamp)
    def aDataspace = 'a-dataspace'
    def aSchemaSet = 'a-schema-set'
    def anAnchor = 'an-anchor'
    def aDataName = 'a-data-name'
    def aDataValue = 'a-data-value'
    def event =
            new CpsDataUpdatedEvent()
                    .withId(UUID.randomUUID().toString())
                    .withSource(eventSource)
                    .withType(eventType)
                    .withContent(
                            new Content()
                                    .withObservedTimestamp(timestampStr)
                                    .withDataspaceName(aDataspace)
                                    .withSchemaSetName(aSchemaSet)
                                    .withAnchorName(anAnchor)
                                    .withData(new Data().withAdditionalProperty(aDataName, aDataValue)))

    def sqlCount = "select count(*) from network_data"
    def sqlStar =  "select * from network_data"
    def sqlWhereClause =
            " where observed_timestamp = to_timestamp(?, 'YYYY-MM-DD\"T\"HH24:MI:SS.USTZHTZM') " +
                    "and dataspace = ? " +
                    "and schema_set = ? " +
                    "and anchor = ?"
    def sqlCountWithConditions = sqlCount + sqlWhereClause
    def sqlStarWithConditions = sqlStar + sqlWhereClause

    def condition = new PollingConditions(timeout: 10, initialDelay: 1, factor: 2)

    def 'Processing a valid event'() {
        given: "no event has been proceeded"
            assert (
                    jdbcTemplate.queryForObject(
                            sqlCountWithConditions, Integer.class, timestampStr, aDataspace, aSchemaSet, anAnchor)
                            == 0)
        when: 'an event is produced'
            this.kafkaTemplate.send(topic, event)
        then: 'the event is proceeded'
            condition.eventually {
                assert (
                        jdbcTemplate.queryForObject(
                                sqlCountWithConditions, Integer.class, timestampStr, aDataspace, aSchemaSet, anAnchor)
                                == 1)
            }
            Map<String, Object> result =
                    jdbcTemplate.queryForMap(sqlStarWithConditions, timestampStr, aDataspace, aSchemaSet, anAnchor)
            log.debug("Data retrieved from db: {}", result)
    }

    def 'Processing an invalid event'() {
        given: 'the number of network data records if known'
            def numberOfRecords = jdbcTemplate.queryForObject(sqlCount, Integer.class)
        when: 'an invalid event is produced'
            this.kafkaTemplate.send(topic, (CpsDataUpdatedEvent) null)
        then: 'the event is not proceeded and no more network data record is created'
            Thread.sleep(1000)
            assert (jdbcTemplate.queryForObject(sqlCount, Integer.class) == numberOfRecords)
    }

    @TestConfiguration
    static class KafkaTestContainersConfiguration {

        @Bean
        ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>()
            factory.setConsumerFactory(consumerFactory())
            return factory
        }

        @Bean
        ProducerFactory<String, String> producerFactory() {
            Map<String, Object> props = new HashMap<>()
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.getBootstrapServers())
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)
            return new DefaultKafkaProducerFactory<>(props)
        }

        @Bean
        ConsumerFactory<Integer, String> consumerFactory() {
            Map<String, Object> props = new HashMap<>()
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.getBootstrapServers())
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'earliest')
            props.put(ConsumerConfig.GROUP_ID_CONFIG, 'my-group')
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class)
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class)
            props.put('spring.deserializer.key.delegate.class', StringDeserializer.class)
            props.put('spring.deserializer.value.delegate.class', JsonDeserializer.class)
            props.put('spring.json.value.default.type', CpsDataUpdatedEvent.class)
            return new DefaultKafkaConsumerFactory<>(props)
        }

        @Bean
        KafkaTemplate<String, CpsDataUpdatedEvent> kafkaTemplate() {
            return new KafkaTemplate<String, CpsDataUpdatedEvent>(producerFactory()
                    as ProducerFactory<String, CpsDataUpdatedEvent>)
        }

    }

}
