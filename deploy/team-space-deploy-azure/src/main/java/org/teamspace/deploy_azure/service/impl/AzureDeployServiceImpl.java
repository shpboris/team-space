package org.teamspace.deploy_azure.service.impl;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teamspace.deploy_azure.arm.service.DeploymentManagerService;
import org.teamspace.deploy_azure.service.AzureDeployService;
import org.teamspace.deploy_common.domain.DeployRequest;
import org.teamspace.deploy_common.domain.DeployResponse;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class AzureDeployServiceImpl implements AzureDeployService {

    public static final String AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-template.json";

    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {

        log.info("Started deployment to Azure");

        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                "96f38b30-1a9e-471f-b208-3a5126f58264",
                "945c199a-83a2-4e80-9f8c-5a91be5752dd",
                "8XRXXPfnep04YSJmtPEFsLUCI69pV3nHvu4UJZoS3mw=", AzureEnvironment.AZURE);
        Azure azure = Azure.authenticate(credentials).withSubscription("a5490867-d17a-4fa2-9f81-1e1bcb2d4a2d");

        String deploymentName = "borissDeploymentName3";
        String rgName = "boriss-rg-3";
        Map<String, ParameterValue> params = new HashMap<String, ParameterValue>();
        params.put("storageNamePrefix", new ParameterValue("borisstore3"));
        String storageAccountOutputKey = "storageAccountName";

        createResourceGroup(azure, Region.US_EAST, rgName);

        deploymentManagerService.deleteDeployment(azure, rgName, deploymentName);

        deploymentManagerService.createDeployment(azure, rgName, deploymentName,
                AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION, params);

        Deployment deployment = deploymentManagerService.waitForDeploymentCreation(azure,
                rgName, deploymentName, 20);

        String storageAccountOutputValue = deploymentManagerService.getDeploymentOutput(azure,
                deployment, storageAccountOutputKey);
        log.info("Storage account name is: {}", storageAccountOutputValue);

        log.info("Completed deployment to Azure");

        return null;
    }

    public ResourceGroup createResourceGroup(Azure azure, Region region, String rgName) {
        return azure.resourceGroups().define(rgName)
                .withRegion(region)
                .create();
    }

    public void deleteResourceGroup(Azure azure, Region region, String rgName) {
        azure.resourceGroups().deleteByName(rgName);
        return;
    }
}
