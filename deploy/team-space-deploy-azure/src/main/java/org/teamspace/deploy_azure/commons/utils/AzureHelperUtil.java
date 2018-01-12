package org.teamspace.deploy_azure.commons.utils;

import com.microsoft.azure.management.storage.StorageAccount;

import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.CENTOS_CUSTOM_DATA_CMD_LINE_TEMPLATE;

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

    public static final String getCmdLine(String domain, String subscription, String client, String secret){
        String cmdLine = String.format(CENTOS_CUSTOM_DATA_CMD_LINE_TEMPLATE, domain, subscription, client, secret);
        return cmdLine;
    }
}
