package org.teamspace.instance.service;

import org.teamspace.deploy_common.domain.AddInstanceEnterpriseModeRequest;
import org.teamspace.deploy_common.domain.RemoveInstanceEnterpriseModeRequest;
import org.teamspace.instance.domain.*;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface InstanceManager {
    CreateInstancesResponse createInstance(CreateInstancesRequest createInstanceRequest);
    void destroyInstance(DestroyInstanceRequest destroyInstanceRequest);
    void addInstance(AddInstanceEnterpriseModeRequest addInstanceEnterpriseModeRequest);
    void removeInstance(RemoveInstanceEnterpriseModeRequest removeInstanceEnterpriseModeRequest);
    int getRunningInstancesCount(String envTag);
}
