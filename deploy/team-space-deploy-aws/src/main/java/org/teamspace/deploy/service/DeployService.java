package org.teamspace.deploy.service;

import org.teamspace.deploy_common.domain.*;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface DeployService {
    DeployResponse deploy(DeployEnterpriseModeRequest deployEnterpriseModeRequest);
    DeployResponse deploy(DeployRequest deployRequest);
    void undeploy(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest);
    void undeploy(UndeployRequest undeployRequest);
    void addInstance(AddInstanceEnterpriseModeRequest addInstanceEnterpriseModeRequest);
    void removeInstance(RemoveInstanceEnterpriseModeRequest removeInstanceEnterpriseModeRequest);
    InstancesDetailsEnterpriseModeResponse getInstancesDetails(InstancesDetailsEnterpriseModeRequest instancesDetailsEnterpriseModeRequest);
}
