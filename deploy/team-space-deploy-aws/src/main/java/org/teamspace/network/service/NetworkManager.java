package org.teamspace.network.service;

import org.teamspace.network.domain.CreateNetworkRequest;
import org.teamspace.network.domain.CreateNetworkResponse;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface NetworkManager {
    CreateNetworkResponse createNetwork(CreateNetworkRequest createNetworkRequest);
}
