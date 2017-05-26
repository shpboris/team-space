package org.teamspace.network.service;

import org.teamspace.network.domain.*;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface NetworkManager {
    CreateNetworkResponse createNetwork(CreateNetworkRequest createNetworkRequest);
    void destroyNetwork(DestroyNetworkRequest destroyNetworkRequest);
}
