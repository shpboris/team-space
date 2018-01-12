package org.teamspace.deploy_azure.storage.service.impl;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.client.context.AzureContext;
import org.teamspace.deploy_azure.storage.service.StorageHandlerService;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.EnumSet;

import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.CONCURRENT_UPLOAD_THREADS_COUNT;
import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.SAS_URI_METADATA_KEY;
import static org.teamspace.deploy_azure.commons.utils.AzureHelperUtil.getStorageConnectionString;

@Service
@Slf4j
public class StorageHandlerServiceImpl implements StorageHandlerService {

    @Value("${artifactsDir}")
    private String artifactsDir;

    public String uploadArtifactFile(CloudBlobContainer cloudBlobContainer,
                                     String fullArtifactName) throws Exception {
        log.info("Started artifact upload to container: {}", cloudBlobContainer.getName());
        String sasUri = obtainArtifactSasUri(cloudBlobContainer, fullArtifactName);

        log.info("Proceeding to artifact upload");
        BlobRequestOptions options = new BlobRequestOptions();
        options.setConcurrentRequestCount(CONCURRENT_UPLOAD_THREADS_COUNT);

        CloudBlockBlob blob = new CloudBlockBlob(new URI(sasUri));
        blob.uploadFromFile(artifactsDir + "/" + fullArtifactName,
                AccessCondition.generateEmptyCondition(),
                options,
                new OperationContext());
        log.info("Artifact was uploaded, proceeding to metadata upload");
        blob.getMetadata().put(SAS_URI_METADATA_KEY, sasUri);
        blob.uploadMetadata();
        log.info("Completed artifact upload to container: {}", cloudBlobContainer.getName());
        return sasUri;
    }

    public String uploadArtifactData(CloudBlobContainer cloudBlobContainer,
                                     String fullArtifactName, String artifactData) throws Exception {
        log.info("Started artifact upload to container: {}", cloudBlobContainer.getName());
        String sasUri = obtainArtifactSasUri(cloudBlobContainer, fullArtifactName);
        log.info("Proceeding to artifact upload");
        CloudBlockBlob blob = new CloudBlockBlob(new URI(sasUri));
        blob.uploadFromByteArray(artifactData.getBytes(), 0, artifactData.length());
        log.info("Artifact was uploaded, proceeding to metadata upload");
        blob.getMetadata().put(SAS_URI_METADATA_KEY, sasUri);
        blob.uploadMetadata();
        log.info("Completed artifact upload to container: {}", cloudBlobContainer.getName());
        return sasUri;
    }

    public String locateArtifact(String resourceGroupName, String storageAccountName,
                                 String blobContainerName, String artifactName)  {
        String sasUri = null;
        try {
            StorageAccount storageAccount = findStorageAccount(resourceGroupName, storageAccountName);
            String storageConnectionString = getStorageConnectionString(storageAccount);
            CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient cloudBlobClient = cloudStorageAccount.createCloudBlobClient();
            CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(blobContainerName);
            CloudBlockBlob blobReference = cloudBlobContainer.getBlockBlobReference(artifactName);
            blobReference.downloadAttributes();
            sasUri = blobReference.getMetadata().get(SAS_URI_METADATA_KEY);
        } catch (Exception e){
            log.error("Failed to locate artifact {}", artifactName);
            throw new RuntimeException("Failed to locate artifact", e);
        }
        return sasUri;
    }


    public CloudBlobContainer createBlobContainer(StorageAccount storageAccount,
                                                  String blobContainerName) throws Exception {
        log.info("Started create of blob container: {}", blobContainerName);
        String storageConnectionString = getStorageConnectionString(storageAccount);
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient cloudBlobClient = cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(blobContainerName);
        //cloudBlobContainer.downloadPermissions().setPublicAccess(BlobContainerPublicAccessType.BLOB);
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

    private String obtainArtifactSasUri(CloudBlobContainer cloudBlobContainer,
                                  String fullArtifactName) throws Exception {
        CloudBlockBlob blobReference = cloudBlobContainer.getBlockBlobReference(fullArtifactName);
        SharedAccessBlobPolicy itemPolicy = getSharedAccessBlobPolicy();
        String sasToken = blobReference.generateSharedAccessSignature(itemPolicy, null,
                null, null, SharedAccessProtocols.HTTPS_ONLY);
        String sasUri = String.format("%s?%s", blobReference.getUri(), sasToken);
        sasUri = sasUri.replace("http://", "https://");
        log.debug("Full blob URL is: " + sasUri);
        return sasUri;
    }

    private SharedAccessBlobPolicy getSharedAccessBlobPolicy(){
        SharedAccessBlobPolicy itemPolicy = new SharedAccessBlobPolicy();

        itemPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE));

        /*LocalDateTime now = LocalDateTime.now();
        Instant result = now.minusDays(7).atZone(ZoneOffset.UTC).toInstant();
        Date startTime = Date.from(result);
        itemPolicy.setSharedAccessStartTime(startTime);*/

        LocalDateTime now = LocalDateTime.now();
        Instant result = now.plusDays(7).atZone(ZoneOffset.UTC).toInstant();
        Date expirationTime = Date.from(result);
        itemPolicy.setSharedAccessExpiryTime(expirationTime);

        return itemPolicy;
    }

}
