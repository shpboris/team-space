package org.teamspace.network.service;

import org.teamspace.network.domain.DestroyNetworkRequest;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface NetworkDestroyer {
    void destroyNetwork(DestroyNetworkRequest destroyNetworkRequest);
}
