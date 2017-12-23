package org.teamspace.deploy_azure.client;

import com.microsoft.azure.management.Azure;

public interface AzureClientFactory {
    Azure getAzureClient();
    String getDomain();
    String getSubscription();
    String getClient();
    String getSecret();
}
