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

package org.onap.cps.temporal.service;

import java.util.Arrays;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.repository.NetworkDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service implementation for Network Data.
 */
@Component
@Slf4j
public class NetworkDataServiceImpl implements NetworkDataService {

    @Autowired
    NetworkDataRepository networkDataRepository;

    @Override
    public NetworkData addNetworkData(final NetworkData networkData) {
        return networkDataRepository.save(networkData);
    }

    @Override
    public Collection<NetworkData> getNetworkData(final String dataspaceName, final String schemaSet,
        final String anchor, final String payload) {
        return Arrays.asList(NetworkData.builder().build());
    }
}
