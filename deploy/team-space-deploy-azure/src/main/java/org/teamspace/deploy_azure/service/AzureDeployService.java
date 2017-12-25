package org.teamspace.deploy_azure.service;


import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;
import org.teamspace.deploy_common.domain.UndeployRequest;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface AzureDeployService {
    public DeployResponse deploy(DeployRequest deployRequest);
    public void undeploy(UndeployRequest undeployRequest);
}
