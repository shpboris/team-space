package org.teamspace.deploy_azure.storage.service.impl;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.client.context.AzureContext;
import org.teamspace.deploy_azure.storage.service.StorageHandlerService;

import java.io.File;
import java.io.FileInputStream;

import static org.teamspace.deploy_azure.commons.utils.AzureHelperUtil.getStorageConnectionString;

@Service
@Slf4j
public class StorageHandlerServiceImpl implements StorageHandlerService {

    @Value("${artifactsDir}")
    private String artifactsDir;

    public void uploadArtifact(CloudBlobContainer cloudBlobContainer,
                               String fullArtifactName) throws Exception {
        log.info("Started artifact upload to container: {}", cloudBlobContainer.getName());
        CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(fullArtifactName);
        File file = new File(artifactsDir + "/" + fullArtifactName);
        log.info("Proceeding to artifact upload, artifact size: {}", file.length());
        blob.upload(new FileInputStream(file), file.length());
        log.info("Completed artifact upload to container: {}", cloudBlobContainer.getName());
    }


    public CloudBlobContainer createBlobContainer(StorageAccount storageAccount,
                                                  String blobContainerName) throws Exception {
        log.info("Started create of blob container: {}", blobContainerName);
        String storageConnectionString = getStorageConnectionString(storageAccount);
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient cloudBlobClient = cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(blobContainerName);
        cloudBlobContainer.createIfNotExists();
        log.info("Completed create of blob container: {}", blobContainerName);
        return cloudBlobContainer;
    }

    public StorageAccount findStorageAccount(String resourceGroupName, String storageAccountName){
        log.info("Started to search for storage account: {} in resource group: {}",
                storageAccountName, resourceGroupName);
        StorageAccount storageAccount = AzureContext.getAzureClient().storageAccounts()
                .getByResourceGroup(resourceGroupName, storageAccountName);
        log.info("Completed to search for storage account: {} in resource group: {}",
                storageAccountName, resourceGroupName);
        log.info("Storage account existence flag is: {}", storageAccount != null ? true : false);
        return storageAccount;
    }

    public StorageAccount createStorageAccount(String resourceGroupName, String storageAccountName){
        log.info("Started create of storage account: {} in region: {}",
                storageAccountName, AzureContext.getRegion().name());
        StorageAccount storageAccount = AzureContext.getAzureClient()
                .storageAccounts().define(storageAccountName)
                .withRegion(AzureContext.getRegion())
                .withNewResourceGroup(resourceGroupName)
                .create();
        log.info("Completed create of storage account: {} in region: {}",
                storageAccountName, AzureContext.getRegion().name());
        return storageAccount;
    }

}
