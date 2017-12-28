package org.teamspace.deploy_azure.service.impl;

import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.arm.service.DeploymentManagerService;
import org.teamspace.deploy_azure.client.AzureClientFactory;
import org.teamspace.deploy_azure.client.context.AzureContext;
import org.teamspace.deploy_azure.commons.components.CustomDataHelper;
import org.teamspace.deploy_azure.commons.params.ParameterValue;
import org.teamspace.deploy_azure.commons.utils.AzureHelperUtil;
import org.teamspace.deploy_azure.resource_group.service.ResourceGroupHandlerService;
import org.teamspace.deploy_azure.service.AzureDeployService;
import org.teamspace.deploy_azure.storage.service.StorageHandlerService;
import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;
import org.teamspace.deploy_common.domain.UndeployRequest;

import java.util.HashMap;
import java.util.Map;

import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.*;
import static org.teamspace.deploy_azure.commons.utils.AzureEntitiesHelperUtil.*;
import static org.teamspace.deploy_common.constants.DeployCommonConstants.DB_MODE_H2;
import static org.teamspace.deploy_common.constants.DeployCommonConstants.OVERRIDE_EXISTING_ARTIFACT;


@Service
@Slf4j
public class AzureDeployServiceImpl implements AzureDeployService {

    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @Autowired
    private StorageHandlerService storageHandlerService;

    @Autowired
    private ResourceGroupHandlerService resourceGroupHandlerService;

    @Autowired
    private AzureClientFactory azureClientFactory;

    @Autowired
    private CustomDataHelper customDataHelper;

    @Value("${artifactsDir}")
    private String artifactsDir;

    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        log.info("Started deployment to Azure");
        initAzureContext(deployRequest.getRegion());
        DeployResponse deployResponse = null;
        try {
            if(OVERRIDE_EXISTING_ARTIFACT){
                syncArtifact(deployRequest);
            }
            recreateResourceGroup(deployRequest);
            Deployment deployment = createDeployment(deployRequest);
            String publicDns = deploymentManagerService.getDeploymentOutput(deployment, DEPLOYMENT_OUTPUT_PUBLIC_DNS);
            log.info("Public DNS is: {}", publicDns);
            deployResponse = new DeployResponse(publicDns, null);
        } catch (Exception e){
            throw new RuntimeException("Deployment to Azure failed", e);
        }
        destroyAzureContext();
        log.info("Completed deployment to Azure");
        return deployResponse;
    }

    @Override
    public void undeploy(UndeployRequest undeployRequest) {
        log.info("Started undeploy from Azure");
        initAzureContext(undeployRequest.getRegion());
        if(undeployRequest.isDeleteArtifact()){
            String storageResourceGroupName = getStorageResourceGroupName(undeployRequest.getEnvTag());
            resourceGroupHandlerService.deleteResourceGroup(storageResourceGroupName);
        }
        String resourceGroupName = getResourceGroupName(undeployRequest.getEnvTag());
        resourceGroupHandlerService.deleteResourceGroup(resourceGroupName);
        destroyAzureContext();
        log.info("Completed undeploy from Azure");
    }

    private void recreateResourceGroup(DeployRequest deployRequest){
        String resourceGroupName = getResourceGroupName(deployRequest.getEnvTag());
        resourceGroupHandlerService.deleteResourceGroup(resourceGroupName);
        resourceGroupHandlerService.createResourceGroup(resourceGroupName);
    }

    private void syncArtifact(DeployRequest deployRequest) throws Exception{
        String storageResourceGroupName = getStorageResourceGroupName(deployRequest.getEnvTag());
        String storageAccountName = getStorageAccountName(deployRequest.getEnvTag());
        String blobContainerName = getContainerName(deployRequest.getEnvTag());
        String fullArtifactName = getFullArtifactName(deployRequest);

        resourceGroupHandlerService.deleteResourceGroup(storageResourceGroupName);
        resourceGroupHandlerService.createResourceGroup(storageResourceGroupName);
        StorageAccount storageAccount = storageHandlerService.
                createStorageAccount(storageResourceGroupName, storageAccountName);
        CloudBlobContainer cloudBlobContainer = storageHandlerService.
                createBlobContainer(storageAccount, blobContainerName);
        storageHandlerService.uploadArtifact(cloudBlobContainer, fullArtifactName);
    }

    private Deployment createDeployment(DeployRequest deployRequest){

        String deploymentName = getDeploymentName(deployRequest.getEnvTag());
        String resourceGroupName = getResourceGroupName(deployRequest.getEnvTag());
        Map<String, ParameterValue> params = getDeploymentParameters(deployRequest);
        String templateClasspathLocation = null;
        if(deployRequest.getDbMode().equals(DB_MODE_AZ_MYSQL)){
            templateClasspathLocation = AZ_MYSQL_MODE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION;
        } else if (deployRequest.getDbMode().equals(DB_MODE_H2)){
            templateClasspathLocation = H2_MODE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION;
        }
        deploymentManagerService.createDeployment(resourceGroupName, deploymentName,
                templateClasspathLocation, params);

        Deployment deployment = deploymentManagerService.waitForDeploymentCreation(resourceGroupName,
                deploymentName, ARM_DEPLOYMENT_MAX_RETRIES);
        return deployment;
    }

    private String getCustomData(DeployRequest deployRequest){
        String storageResourceGroupName = getStorageResourceGroupName(deployRequest.getEnvTag());
        String storageAccountName = getStorageAccountName(deployRequest.getEnvTag());
        String containerName = getContainerName(deployRequest.getEnvTag());
        StorageAccount storageAccount = storageHandlerService.
                findStorageAccount(storageResourceGroupName, storageAccountName);
        String storageConnectionStr = AzureHelperUtil.getStorageConnectionString(storageAccount);
        String customData = customDataHelper.
                getCustomDataScript(deployRequest.getEnvTag(),deployRequest.getArtifactName(), deployRequest.getUser(),
                        deployRequest.getPassword(), deployRequest.getDbMode(), AzureContext.getDomain(),
                        AzureContext.getSubscription(), AzureContext.getClient(),
                        AzureContext.getSecret(), storageConnectionStr, containerName);
        return customData;
    }

    private Map<String, ParameterValue> getDeploymentParameters(DeployRequest deployRequest){
        Map<String, ParameterValue> params = new HashMap<String, ParameterValue>();
        String customData = getCustomData(deployRequest);
        params.put(ADMIN_USERNAME_KEY, new ParameterValue(deployRequest.getUser()));
        params.put(ADMIN_PASSWORD_KEY, new ParameterValue(deployRequest.getPassword()));
        params.put(CUSTOM_DATA_KEY, new ParameterValue(customData));
        params.put(ENV_TAG_KEY, new ParameterValue(deployRequest.getEnvTag()));
        return params;
    }

    private String getFullArtifactName(DeployRequest deployRequest){
        return deployRequest.getArtifactName() + ".tar.gz";
    }

    private void initAzureContext(String region){
        AzureContext.init(region, azureClientFactory);
    }

    private void destroyAzureContext(){
        AzureContext.destroy();
    }
}
