package org.teamspace.deploy_azure.arm.service;

import com.microsoft.azure.management.resources.Deployment;
import org.teamspace.deploy_azure.commons.params.ParameterValue;

import java.util.Map;

public interface DeploymentManagerService {
    void createDeployment(String resourceGroup,
                          String deploymentName,
                          String templateClassPathLocation, Map<String, ParameterValue> params);
    Deployment waitForDeploymentCreation(String resourceGroup,
                                         String deploymentName, int maxRetriesCount);
    String getDeploymentOutput(Deployment deployment, String outputKey);
    void deleteDeployment(String resourceGroup,
                                 String deploymentName);
}
