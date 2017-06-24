package org.teamspace.instance.service;

import org.teamspace.instance.domain.*;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface InstanceManager {
    CreateInstancesResponse createInstance(CreateInstancesRequest createInstanceRequest);
    void destroyInstance(DestroyInstanceRequest destroyInstanceRequest);
}
