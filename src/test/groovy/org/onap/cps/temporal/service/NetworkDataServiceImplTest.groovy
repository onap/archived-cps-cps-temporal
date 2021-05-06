package org.onap.cps.temporal.service

import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.OffsetDateTime

class NetworkDataServiceImplTest extends Specification {

    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.now()
    private static final String DATASPACE_NAME = "TEST_DATASPACE"
    private static final String SCHEMA_SET_NAME = "TEST_SCHEMA_SET"
    private static final String ANCHOR_NAME = "TEST_ANCHOR"
    private static final String PAYLOAD = "{ \"message\": \"Hello World!\" }"
    private static final OffsetDateTime VERSION = OffsetDateTime.now()

    def objectUnderTest = new NetworkDataServiceImpl()

    def 'Add network data in timeseries database.'() {
        when: 'a new network data is added'
            def result = objectUnderTest.addNetworkData(DATASPACE_NAME, SCHEMA_SET_NAME, ANCHOR_NAME, PAYLOAD)
        then: ' added network data is returned'
            assert result != null
    }

    void 'Query a network data by %scenario.'() {
        when:
            def result = objectUnderTest.getNetworkData(dataspace_name, schema_set_name, anchor_name, payload)
        then:
            assert result != null
        where: 'the following data is used'
            scenario                               | dataspace_name   | schema_set_name | anchor_name | payload
            'dataspace name and schemaset name'    | DATASPACE_NAME   | SCHEMA_SET_NAME | ''          | ''
            'dataspace name and anchor name'       | DATASPACE_NAME   | ''              | ANCHOR_NAME | ''
    }
}
