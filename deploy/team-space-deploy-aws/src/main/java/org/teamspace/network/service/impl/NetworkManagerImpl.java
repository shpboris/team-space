package org.teamspace.network.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.network.domain.CreateNetworkRequest;
import org.teamspace.network.domain.CreateNetworkResponse;
import org.teamspace.network.service.NetworkCreator;
import org.teamspace.network.service.NetworkManager;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class NetworkManagerImpl implements NetworkManager {

    @Autowired
    private NetworkCreator networkCreator;


    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkRequest createNetworkRequest) {
        CreateNetworkResponse createNetworkResponse = networkCreator.createNetwork(createNetworkRequest);
        return createNetworkResponse;
    }


}
