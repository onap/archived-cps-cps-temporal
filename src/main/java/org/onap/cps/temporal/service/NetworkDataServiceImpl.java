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

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.NetworkDataId;
import org.onap.cps.temporal.repository.NetworkDataRepository;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Network Data.
 */
@Service
@Slf4j
public class NetworkDataServiceImpl implements NetworkDataService {

    private final NetworkDataRepository networkDataRepository;

    public NetworkDataServiceImpl(final NetworkDataRepository networkDataRepository) {
        this.networkDataRepository = networkDataRepository;
    }

    @Override
    public NetworkData addNetworkData(final NetworkData networkData) {
        final var result = networkDataRepository.save(networkData);
        if (result.getCreatedTimestamp() == null) {
            // Data already exists and can not be inserted
            final var id =
                    new NetworkDataId(
                            networkData.getObservedTimestamp(), networkData.getDataspace(), networkData.getAnchor());
            final Optional<NetworkData> existing = networkDataRepository.findById(id);
            throw new ServiceException("Network data was already created: " + (existing.orElse(null)));
        }
        return result;
    }

}
