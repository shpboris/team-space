package org.teamspace.deploy_azure.service.impl;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.arm.service.DeploymentManagerService;
import org.teamspace.deploy_azure.service.AzureDeployService;
import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;


import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class AzureDeployServiceImpl implements AzureDeployService {

    public static final String AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-template.json";

    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @Value("${artifactsDir}")
    private String artifactsDir;

    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        try {

            log.info("Started deployment to Azure");

            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                    "96f38b30-1a9e-471f-b208-3a5126f58264",
                    "945c199a-83a2-4e80-9f8c-5a91be5752dd",
                    "8XRXXPfnep04YSJmtPEFsLUCI69pV3nHvu4UJZoS3mw=", AzureEnvironment.AZURE);
            Azure azure = Azure.authenticate(credentials).withSubscription("a5490867-d17a-4fa2-9f81-1e1bcb2d4a2d");


            String deploymentName = getDeploymentName(deployRequest);
            String rgName = getResourceGroupName(deployRequest);
            String storageAccountName = getStorageAccountName(deployRequest);
            Map<String, ParameterValue> params = getParameters(deployRequest);
            Region region = Region.fromName(deployRequest.getRegion());
            String outputKey = "sshCommand";


            deleteResourceGroup(azure, region, rgName);
            createResourceGroup(azure, region, rgName);
            StorageAccount storageAccount = createStorageAccount(azure, region, rgName, storageAccountName);
            uploadArtifact(deployRequest, storageAccount);

            deploymentManagerService.createDeployment(azure, rgName, deploymentName,
                    AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION, params);

            Deployment deployment = deploymentManagerService.waitForDeploymentCreation(azure,
                    rgName, deploymentName, 20);

            String outputKeyValue = deploymentManagerService.getDeploymentOutput(azure,
                    deployment, outputKey);
            log.info("Output is: {}", outputKeyValue);

            log.info("Completed deployment to Azure");
        } catch (Exception e){
            throw new RuntimeException("Deployment to Azure failed", e);
        }

        return null;
    }

    public void uploadArtifact(DeployRequest deployRequest,
                               StorageAccount storageAccount) throws Exception {
        log.info("Started artifact upload to storage account: {}", storageAccount.name());
        String storageConnectionString =
                "DefaultEndpointsProtocol=http;"
                        + "AccountName=%s;"
                        + "AccountKey=%s";

        storageConnectionString =
                String.format(storageConnectionString, storageAccount.name(),
                        storageAccount.getKeys().get(0).value());

        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient cloudBlobClient = cloudStorageAccount.createCloudBlobClient();
        String containerName = getContainerName(deployRequest);
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
        boolean isCreated = cloudBlobContainer.createIfNotExists();
        if(!isCreated){
            log.error("Container: {} wasn't created", containerName);
        } else {
            log.info("Container: {} created successfully", containerName);
        }
        String fullArtifactName = deployRequest.getArtifactName() + ".tar.gz";
        CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(fullArtifactName);
        File file = new File(artifactsDir + "/" + fullArtifactName);
        log.info("Proceeding to artifact upload, artifact size: {}", file.length());
        blob.upload(new FileInputStream(file), file.length());
        log.info("Completed artifact upload to storage account {}", storageAccount.name());
    }

    public StorageAccount createStorageAccount(Azure azure, Region region,
                                               String rgName, String storageAccountName){
        log.info("Started create of storage account: {} in region: {}",
                storageAccountName, region.name());
        StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .create();
        log.info("Completed create of storage account: {} in region: {}",
                storageAccountName, region.name());
        return storageAccount;
    }

    public ResourceGroup createResourceGroup(Azure azure, Region region, String rgName) {
        log.info("Started create of resource group: {} in region: {}", rgName, region.name());
        ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
                .withRegion(region)
                .create();
        log.info("Completed create of resource group: {} in region: {}", rgName, region.name());
        return resourceGroup;
    }

    public void deleteResourceGroup(Azure azure, Region region, String rgName) {
        log.info("Started delete of resource group: {} from region: {}", rgName, region.name());
        if(azure.resourceGroups().getByName(rgName) != null) {
            azure.resourceGroups().deleteByName(rgName);
        }
        log.info("Completed delete of resource group: {} from region: {}", rgName, region.name());
        return;
    }

    private String getDeploymentName(DeployRequest deployRequest){
        String resourceGroupName = deployRequest.getEnvTag() + "-" + "DEPLOYMENT";
        return resourceGroupName;
    }

    private String getResourceGroupName(DeployRequest deployRequest){
        String resourceGroupName = deployRequest.getEnvTag() + "-" + "RESOURCE_GROUP";
        return resourceGroupName;
    }

    private String getStorageAccountName(DeployRequest deployRequest){
        String storageAccountName = deployRequest.getEnvTag() + "storage";
        storageAccountName = storageAccountName.toLowerCase();
        return storageAccountName;
    }

    private String getContainerName(DeployRequest deployRequest){
        String containerName = deployRequest.getEnvTag() + "container";
        containerName = containerName.toLowerCase();
        return containerName;
    }

    private Map<String, ParameterValue> getParameters(DeployRequest deployRequest){
        Map<String, ParameterValue> params = new HashMap<String, ParameterValue>();
        params.put("adminUsername", new ParameterValue(deployRequest.getUser()));
        params.put("adminPassword", new ParameterValue(deployRequest.getPassword()));
        params.put("envTag", new ParameterValue(deployRequest.getEnvTag()));
        return params;
    }
}
