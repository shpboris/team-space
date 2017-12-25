package org.teamspace.deploy_azure.storage.service;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public interface StorageHandlerService {
    StorageAccount createStorageAccount(String resourceGroupName, String storageAccountName);
    StorageAccount findStorageAccount(String resourceGroupName, String storageAccountName);
    CloudBlobContainer createBlobContainer(StorageAccount storageAccount,
                                           String blobContainerName) throws Exception;
    void uploadArtifact(CloudBlobContainer cloudBlobContainer,
                        String fullArtifactName) throws Exception;
}
