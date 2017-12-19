package org.teamspace.deploy_azure.arm.service.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.arm.service.DeploymentManagerService;
import org.teamspace.deploy_azure.service.impl.ParameterValue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DeploymentManagerServiceImpl implements DeploymentManagerService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void createDeployment(Azure azure, String resourceGroupName, String deploymentName, String templateClassPathLocation, Map<String, ParameterValue> params) {
        log.info("Started deployment: {} to resource group: {}", deploymentName, resourceGroupName);
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource(templateClassPathLocation);
            inputStream = resource.getInputStream();
            String azureDeployTemplate = IOUtils.toString(inputStream, "UTF-8");
            azure.deployments().define(deploymentName)
                    .withExistingResourceGroup(resourceGroupName)
                    .withTemplate(azureDeployTemplate)
                    .withParameters(params)
                    .withMode(DeploymentMode.INCREMENTAL)
                    .beginCreate();
        } catch (Exception e){
            log.error("Couldn't deploy template", e);
            throw new RuntimeException("Couldn't deploy template", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Completed deployment: {} to resource group: {}", deploymentName, resourceGroupName);
    }

    @Override
    public Deployment waitForDeploymentCreation(Azure azure, String resourceGroupName, String deploymentName, int maxRetriesCount) {
        log.info("Started waiting for deployment: {} completion in resource group: {}", deploymentName, resourceGroupName);
        Deployment deployment = azure.deployments().getByResourceGroup(resourceGroupName, deploymentName);
        int retiesCount = 0;
        while (retiesCount < maxRetriesCount && !(deployment.provisioningState().equalsIgnoreCase("Succeeded")
                || deployment.provisioningState().equalsIgnoreCase("Failed")
                || deployment.provisioningState().equalsIgnoreCase("Cancelled"))) {
            log.info("Attempt #{}, deployment status is: {}", retiesCount, deployment.provisioningState());
            SdkContext.sleep(30000);
            deployment = azure.deployments().getByResourceGroup(resourceGroupName, deploymentName);
            retiesCount++;
        }
        log.info("Final deployment status is: {}", deployment.provisioningState());
        log.info("Completed waiting for deployment: {} completion in resource group: {}", deploymentName, resourceGroupName);
        return deployment;
    }

    @Override
    public String getDeploymentOutput(Azure azure, Deployment deployment, String outputKey) {
        Map outputs = (Map)deployment.outputs();
        Map param = (Map)outputs.get(outputKey);
        String outputValue = (String)param.get("value");
        log.info("Output value is: {} for key: {}", outputValue, outputKey);
        return outputValue;
    }

    @Override
    public void deleteDeployment(Azure azure, String resourceGroupName, String deploymentName) {
        log.info("Deleting deployment: {} in resource group: {}", deploymentName, resourceGroupName);
        Deployment deployment = azure.deployments().getByResourceGroup(resourceGroupName, deploymentName);
        try {
            deployment.cancel();
        } catch (Exception e){
            log.warn("Failed to cancel deployment: {}", deploymentName);
        }
        try {
            azure.deployments().deleteByResourceGroup(resourceGroupName, deploymentName);
        }catch (Exception e){
            log.warn("Failed to delete deployment: {}", deploymentName);
        }
        log.info("Completed deleting deployment: {} in resource group: {}", deploymentName, resourceGroupName);
    }
}
