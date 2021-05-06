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

import lombok.NonNull;
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
    public NetworkData addNetworkData(@NonNull final String dataspaceName, @NonNull final String schemaSetName,
        @NonNull final String anchorName, @NonNull final String payload) {
        final NetworkData networkData = NetworkData.builder()
            .dataspace(dataspaceName)
            .schemaSet(schemaSetName)
            .anchor(anchorName)
            .payload(payload)
            .build();
        return networkDataRepository.save(networkData);
    }

    @Override
    public NetworkData getNetworkData(@NonNull final String dataspaceName, @NonNull final String schemaSet,
        @NonNull final String anchor, @NonNull final String payload) {
        return NetworkData.builder().build();
    }
}
