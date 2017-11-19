package org.teamspace.deploy.service;

import org.teamspace.deploy.domain.*;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface DeployService {
    public DeployResponse deploy(DeployEnterpriseModeRequest deployEnterpriseModeRequest);
    public DeployResponse deploy(DeployRequest deployRequest);
    public void undeploy(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest);
    public void undeploy(UndeployRequest undeployRequest);
}
