package org.teamspace.deploy_azure.arm.service;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.teamspace.deploy_azure.service.impl.ParameterValue;

import java.util.List;
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

/*    ResourceGroup createResourceGroup(Azure azure, Region region, String resourceGroup);
    ResourceGroup deleteResourceGroup(Azure azure, Region region, String resourceGroup);*/
}
