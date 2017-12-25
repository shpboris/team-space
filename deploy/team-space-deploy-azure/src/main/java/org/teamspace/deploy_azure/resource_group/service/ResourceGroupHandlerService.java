package org.teamspace.deploy_azure.resource_group.service;

import com.microsoft.azure.management.resources.ResourceGroup;

public interface ResourceGroupHandlerService {
    ResourceGroup createResourceGroup(String resourceGroupName);
    void deleteResourceGroup(String resourceGroupName);
}
