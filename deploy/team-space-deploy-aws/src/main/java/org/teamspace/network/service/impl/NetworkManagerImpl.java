package org.teamspace.network.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.network.domain.*;
import org.teamspace.network.service.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class NetworkManagerImpl implements NetworkManager {

    @Autowired
    private NetworkCreator networkCreator;

    @Autowired
    private NetworkDestroyer networkDestroyer;


    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkRequest createNetworkRequest) {
        CreateNetworkResponse createNetworkResponse = networkCreator.createNetwork(createNetworkRequest);
        return createNetworkResponse;
    }

    @Override
    public void destroyNetwork(DestroyNetworkRequest destroyNetworkRequest) {
        networkDestroyer.destroyNetwork(destroyNetworkRequest);
    }


}
