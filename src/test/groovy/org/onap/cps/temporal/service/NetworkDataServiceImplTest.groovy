package org.onap.cps.temporal.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.OffsetDateTime
import org.onap.cps.temporal.domain.NetworkData
import org.onap.cps.temporal.repository.NetworkDataRepository
import spock.lang.Specification

class NetworkDataServiceImplTest extends Specification {

    private static final Gson GSON = new GsonBuilder().create()

    private static final OffsetDateTime OBSERVED_TIMESTAMP = OffsetDateTime.now()
    private static final String DATASPACE_NAME = "TEST_DATASPACE"
    private static final String SCHEMA_SET_NAME = "TEST_SCHEMA_SET"
    private static final String ANCHOR_NAME = "TEST_ANCHOR"
    private static final String PAYLOAD = GSON.toJson("{ \"message\": \"Hello World!\" }")

    def objectUnderTest = new NetworkDataServiceImpl()

    def mockNetworkDataRepository = Mock(NetworkDataRepository)

    def networkData

    def setup() {
        objectUnderTest.networkDataRepository = mockNetworkDataRepository
        networkData = NetworkData.builder()
                .lastModified(OBSERVED_TIMESTAMP)
                .dataspace(DATASPACE_NAME)
                .schemaSet(SCHEMA_SET_NAME)
                .anchor(ANCHOR_NAME)
                .payload(PAYLOAD)
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

    void 'Query a network data by dataspace name and %scenario.'() {
        when: 'network data is fetched'
            def result = objectUnderTest.getNetworkData(DATASPACE_NAME, schema_set_name, anchor_name, payload)
        then: ' retrieved network data is returned'
            assert result != null
        where: 'the following data is used'
            scenario            | schema_set_name | anchor_name | payload
            'schemaset name'    | SCHEMA_SET_NAME | ''          | ''
            'anchor name'       | ''              | ANCHOR_NAME | ''
    }
}
