package org.teamspace.deploy_azure.client.impl;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.teamspace.deploy_azure.client.AzureClientFactory;

@Component
public class AzureClientFactoryImpl implements AzureClientFactory {

    @Value("${domain}")
    private String domain;

    @Value("${subscription}")
    private String subscription;

    @Value("${client}")
    private String client;

    @Value("${secret}")
    private String secret;

    private Azure azure;


    @Override
    public Azure getAzureClient() {
        if(azure == null){
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                    client,
                    domain,
                    secret, AzureEnvironment.AZURE);
            azure = Azure.authenticate(credentials).withSubscription(subscription);
        }
        return azure;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getSubscription() {
        return subscription;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public String getSecret() {
        return secret;
    }
}
