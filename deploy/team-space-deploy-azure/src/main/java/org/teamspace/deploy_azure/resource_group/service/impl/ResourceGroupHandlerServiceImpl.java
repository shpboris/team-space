package org.teamspace.deploy_azure.resource_group.service.impl;

import com.microsoft.azure.management.resources.ResourceGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.client.context.AzureContext;
import org.teamspace.deploy_azure.resource_group.service.ResourceGroupHandlerService;

@Service
@Slf4j
public class ResourceGroupHandlerServiceImpl implements ResourceGroupHandlerService {

    public ResourceGroup createResourceGroup(String resourceGroupName) {
        log.info("Started create of resource group: {} in region: {}", resourceGroupName,
                AzureContext.getRegion().name());
        ResourceGroup resourceGroup = AzureContext.getAzureClient()
                .resourceGroups().define(resourceGroupName)
                .withRegion(AzureContext.getRegion())
                .create();
        log.info("Completed create of resource group: {} in region: {}", resourceGroupName,
                AzureContext.getRegion().name());
        return resourceGroup;
    }

    public void deleteResourceGroup(String resourceGroupName) {
        log.info("Started delete of resource group: {} from region: {}", resourceGroupName,
                AzureContext.getRegion().name());
        if(AzureContext.getAzureClient().resourceGroups().getByName(resourceGroupName) != null) {
            AzureContext.getAzureClient().resourceGroups().deleteByName(resourceGroupName);
        }
        log.info("Completed delete of resource group: {} from region: {}", resourceGroupName,
                AzureContext.getRegion().name());
        return;
    }
}
