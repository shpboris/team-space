package org.teamspace.deploy_azure.client.context;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.teamspace.deploy_azure.client.AzureClientFactory;
import org.teamspace.deploy_common.domain.DeployRequest;

public class AzureContext {

    private static String domain;

    private static String subscription;

    private static String client;

    private static String secret;

    private static Azure azureClient = null;

    private static final ThreadLocal<Region> region = new ThreadLocal<Region>();

    public static void init(DeployRequest deployRequest, AzureClientFactory azureClientFactory){
        domain = azureClientFactory.getDomain();
        subscription = azureClientFactory.getSubscription();
        client = azureClientFactory.getClient();
        secret = azureClientFactory.getSecret();
        azureClient = azureClientFactory.getAzureClient();
        region.set(Region.fromName(deployRequest.getRegion()));
    }

    public static void destroy(){
        region.remove();
    }

    public static Azure getAzureClient(){
        return azureClient;
    }

    public static String getDomain() {
        return domain;
    }

    public static String getSubscription() {
        return subscription;
    }

    public static String getClient() {
        return client;
    }

    public static String getSecret() {
        return secret;
    }

    public static Region getRegion() {
        return region.get();
    }
}
