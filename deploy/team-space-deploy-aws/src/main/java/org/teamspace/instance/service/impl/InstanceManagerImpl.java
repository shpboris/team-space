package org.teamspace.instance.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.instance.domain.*;
import org.teamspace.instance.service.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class InstanceManagerImpl implements InstanceManager {

    @Autowired
    private InstanceCreator instanceCreator;

    @Autowired
    private InstanceDestroyer instanceDestroyer;

    @Override
    public CreateInstanceResponse createInstance(CreateInstanceRequest createInstanceRequest) {
        CreateInstanceResponse createInstanceResponse = instanceCreator.createInstance(createInstanceRequest);
        return createInstanceResponse;
    }

    @Override
    public void destroyInstance(DestroyInstanceRequest destroyInstanceRequest) {
        instanceDestroyer.destroyInstance(destroyInstanceRequest);
    }
}
