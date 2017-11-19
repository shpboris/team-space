package org.teamspace.enterprise_setup.service;

import org.teamspace.deploy.domain.DeployEnterpriseModeRequest;
import org.teamspace.deploy.domain.DeployResponse;
import org.teamspace.deploy.domain.UndeployRequest;

public interface EnterpriseDeployManager {
    DeployResponse createEnvironment(DeployEnterpriseModeRequest deployEnterpriseModeRequest);
    void destroyEnvironment(UndeployRequest undeployRequest);
}
