package org.onap.cps.temporal.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.OffsetDateTime
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import spock.lang.Specification

class NetworkDataServiceImplTest extends Specification {

    private static final Gson GSON = new GsonBuilder().create()

    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.now()
    private static final String DATASPACE_NAME = "TEST_DATASPACE"
    private static final String SCHEMA_SET_NAME = "TEST_SCHEMA_SET"
    private static final String ANCHOR_NAME = "TEST_ANCHOR"
    private static final String PAYLOAD = GSON.toJson("{ \"message\": \"Hello World!\" }")
    private static final OffsetDateTime VERSION = OffsetDateTime.now()

    def objectUnderTest = new NetworkDataServiceImpl()

    def mockNetworkDataRepository = Mock(NetworkDataRepository)

    def networkData

    def setup() {
        objectUnderTest.networkDataRepository = mockNetworkDataRepository
        networkData = NetworkData.builder()
                .timestamp(TIMESTAMP)
                .dataspace(DATASPACE_NAME)
                .schemaSet(SCHEMA_SET_NAME)
                .anchor(ANCHOR_NAME)
                .payload(PAYLOAD)
                .version(VERSION)
                .build()
    }

    def 'Add network data in timeseries database.'() {
        when: 'a new network data is added'
            def result = objectUnderTest.addNetworkData(networkData)
        then: ' added network data is returned'
            1 * mockNetworkDataRepository.save({def networkData ->
                networkData.dataspace == DATASPACE_NAME
                networkData.payload == PAYLOAD})
    }

    void 'Query a network data by %scenario.'() {
        when: 'network data is fetched'
            def result = objectUnderTest.getNetworkData(dataspace_name, schema_set_name, anchor_name, payload)
        then: ' retrieved network data is returned'
            assert result != null
        where: 'the following data is used'
            scenario                               | dataspace_name   | schema_set_name | anchor_name | payload
            'dataspace name and schemaset name'    | DATASPACE_NAME   | SCHEMA_SET_NAME | ''          | ''
            'dataspace name and anchor name'       | DATASPACE_NAME   | ''              | ANCHOR_NAME | ''
    }
}
