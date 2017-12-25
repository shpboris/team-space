package org.teamspace.deploy_azure.commons.utils;

import com.microsoft.azure.management.storage.StorageAccount;

public class AzureHelperUtil {

    public static String getStorageConnectionString(StorageAccount storageAccount){
        String storageConnectionString =
                "DefaultEndpointsProtocol=http;"
                        + "AccountName=%s;"
                        + "AccountKey=%s";
        storageConnectionString =
                String.format(storageConnectionString, storageAccount.name(),
                        storageAccount.getKeys().get(0).value());
        return storageConnectionString;
    }
}
