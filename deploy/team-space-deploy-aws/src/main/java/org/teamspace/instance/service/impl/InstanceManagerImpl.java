package org.teamspace.instance.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.instance.domain.CreateInstanceRequest;
import org.teamspace.instance.domain.CreateInstanceResponse;
import org.teamspace.instance.service.InstanceCreator;
import org.teamspace.instance.service.InstanceManager;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class InstanceManagerImpl implements InstanceManager {

    @Autowired
    private InstanceCreator instanceCreator;

    @Override
    public CreateInstanceResponse createInstance(CreateInstanceRequest createInstanceRequest) {
        CreateInstanceResponse createInstanceResponse = instanceCreator.createInstance(createInstanceRequest);
        return createInstanceResponse;
    }
}
