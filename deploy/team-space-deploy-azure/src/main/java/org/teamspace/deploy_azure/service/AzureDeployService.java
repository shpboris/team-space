package org.teamspace.deploy_azure.service;


import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface AzureDeployService {
    //public DeployResponse deploy(DeployEnterpriseModeRequest deployEnterpriseModeRequest);
    public DeployResponse deploy(DeployRequest deployRequest);
    /*public void undeploy(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest);
    public void undeploy(UndeployRequest undeployRequest);*/
}
