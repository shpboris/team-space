package org.teamspace.deploy_azure.commons.utils;

public class AzureEntitiesHelperUtil {

    public static String getDeploymentName(String envTag){
        String resourceGroupName = envTag + "-" + "DEPLOYMENT";
        return resourceGroupName;
    }

    public static String getResourceGroupName(String envTag){
        String resourceGroupName = envTag + "-" + "RESOURCE_GROUP";
        return resourceGroupName;
    }

    public static String getStorageResourceGroupName(String envTag){
        String storageResourceGroupName = envTag + "-" + "STORAGE_RESOURCE_GROUP";
        return storageResourceGroupName;
    }

    public static String getStorageAccountName(String envTag){
        String storageAccountName = envTag + "storage";
        storageAccountName = storageAccountName.toLowerCase();
        return storageAccountName;
    }

    public static String getContainerName(String envTag){
        String containerName = envTag + "container";
        containerName = containerName.toLowerCase();
        return containerName;
    }

}
