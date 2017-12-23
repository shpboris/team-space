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
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.arm.service.DeploymentManagerService;
import org.teamspace.deploy_azure.service.AzureDeployService;
import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.teamspace.deploy_common.constants.DeployCommonConstants.OVERRIDE_EXISTING_ARTIFACT;


@Slf4j
@Service
public class AzureDeployServiceImpl implements AzureDeployService {

    public static final String AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-template.json";
    public static final String AZURE_CUSTOM_DATA_CLASSPATH_LOCATION = "classpath:scripts/custom_data.sh";
    public static final String TAR_FILE_NAME = "$tarFileName$";
    public static final String USER = "$user$";
    public static final String DOMAIN = "$domain$";
    public static final String SUBSCRIPTION = "$subscription$";
    public static final String CLIENT = "$client$";
    public static final String SECRET = "$secret$";
    public static final String CONTAINER_NAME = "$container-name$";
    public static final String STORAGE_CONNECTION_STRING = "$storage-connection-string$";

    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${artifactsDir}")
    private String artifactsDir;

    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        try {

            log.info("Started deployment to Azure");

            String domain = "945c199a-83a2-4e80-9f8c-5a91be5752dd";
            String subscription = "a5490867-d17a-4fa2-9f81-1e1bcb2d4a2d";
            String client = "96f38b30-1a9e-471f-b208-3a5126f58264";
            String secret = "8XRXXPfnep04YSJmtPEFsLUCI69pV3nHvu4UJZoS3mw=";

            String deploymentName = getDeploymentName(deployRequest);
            String rgName = getResourceGroupName(deployRequest);
            String storageRgName = getStorageResourceGroupName(deployRequest);
            String storageAccountName = getStorageAccountName(deployRequest);
            Region region = Region.fromName(deployRequest.getRegion());
            String containerName = getContainerName(deployRequest);
            String outputKey = "sshCommand";

            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                    client,
                    domain,
                    secret, AzureEnvironment.AZURE);
            Azure azure = Azure.authenticate(credentials).withSubscription(subscription);

            if(OVERRIDE_EXISTING_ARTIFACT){
                deleteResourceGroup(azure, region, storageRgName);
                createResourceGroup(azure, region, storageRgName);
                StorageAccount storageAccount = createStorageAccount(azure, region, storageRgName, storageAccountName);
                uploadArtifact(deployRequest, storageAccount);
            }

            deleteResourceGroup(azure, region, rgName);
            createResourceGroup(azure, region, rgName);
            StorageAccount storageAccount = findStorageAccount(azure, storageRgName, storageAccountName);
            String customData = getCustomDataScript(deployRequest.getArtifactName(), deployRequest.getUser(),
                    domain, subscription, client, secret, storageAccount, containerName);
            Map<String, ParameterValue> params = getParameters(deployRequest, customData);
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
        String storageConnectionString = getStorageConnectionString(storageAccount);
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

    public StorageAccount findStorageAccount(Azure azure,
                                               String rgName, String storageAccountName){
        log.info("Started to search for storage account: {} in resource group: {}",
                storageAccountName, rgName);
        StorageAccount storageAccount = azure.storageAccounts().getByResourceGroup(rgName, storageAccountName);
        log.info("Completed to search for storage account: {} in resource group: {}",
                storageAccountName, rgName);
        log.info("Storage account existence flag is: {}", storageAccount != null ? true : false);
        return storageAccount;
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

    public String getCustomDataScript(String artifactName, String user,
                                      String domain, String subscription,
                                      String client, String secret, StorageAccount storageAccount,
                                      String containerName){
        log.info("Getting custom data script ...");
        String customDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource(AZURE_CUSTOM_DATA_CLASSPATH_LOCATION);
            inputStream = resource.getInputStream();
            customDataScript = IOUtils.toString(inputStream, "UTF-8");
            customDataScript = customDataScript.replace(USER, user);
            customDataScript = customDataScript.replace(TAR_FILE_NAME, artifactName);
            customDataScript = customDataScript.replace(DOMAIN, domain);
            customDataScript = customDataScript.replace(SUBSCRIPTION, subscription);
            customDataScript = customDataScript.replace(CLIENT, client);
            customDataScript = customDataScript.replace(SECRET, secret);
            customDataScript = customDataScript.replace(CONTAINER_NAME, containerName);
            customDataScript = customDataScript.replace(STORAGE_CONNECTION_STRING, getStorageConnectionString(storageAccount));
        } catch (Exception e){
            throw new RuntimeException("Unable to read custom data", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got custom data script");
        log.debug("\n" + customDataScript);
        return customDataScript;
    }

    private String getStorageConnectionString(StorageAccount storageAccount){
            String storageConnectionString =
                    "DefaultEndpointsProtocol=http;"
                            + "AccountName=%s;"
                            + "AccountKey=%s";
            storageConnectionString =
                    String.format(storageConnectionString, storageAccount.name(),
                            storageAccount.getKeys().get(0).value());
            return storageConnectionString;
    }

    private String getDeploymentName(DeployRequest deployRequest){
        String resourceGroupName = deployRequest.getEnvTag() + "-" + "DEPLOYMENT";
        return resourceGroupName;
    }

    private String getResourceGroupName(DeployRequest deployRequest){
        String resourceGroupName = deployRequest.getEnvTag() + "-" + "RESOURCE_GROUP";
        return resourceGroupName;
    }

    private String getStorageResourceGroupName(DeployRequest deployRequest){
        String storageResourceGroupName = deployRequest.getEnvTag() + "-" + "STORAGE_RESOURCE_GROUP";
        return storageResourceGroupName;
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

    private Map<String, ParameterValue> getParameters(DeployRequest deployRequest, String customData){

        Map<String, ParameterValue> params = new HashMap<String, ParameterValue>();
        params.put("adminUsername", new ParameterValue(deployRequest.getUser()));
        params.put("adminPassword", new ParameterValue(deployRequest.getPassword()));
        params.put("customData", new ParameterValue(customData));
        params.put("envTag", new ParameterValue(deployRequest.getEnvTag()));
        return params;
    }
}
