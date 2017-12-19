package org.teamspace.enterprise_setup.service;

import org.teamspace.deploy_common.domain.DeployEnterpriseModeRequest;
import org.teamspace.deploy_common.domain.DeployResponse;
import org.teamspace.deploy_common.domain.UndeployEnterpriseModeRequest;

public interface EnterpriseDeployManager {
    DeployResponse createEnvironment(DeployEnterpriseModeRequest deployEnterpriseModeRequest);
    void destroyEnvironment(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest);
}
