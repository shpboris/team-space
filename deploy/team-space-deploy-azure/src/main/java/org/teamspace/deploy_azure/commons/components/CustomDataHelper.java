package org.teamspace.deploy_azure.commons.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.*;
import static org.teamspace.deploy_common.constants.DeployCommonConstants.TAR_FILE_NAME;

@Component
@Slf4j
public class CustomDataHelper {

    @Autowired
    private ResourceLoader resourceLoader;

    public String getCustomDataScript(String artifactName, String user,
                                      String domain, String subscription,
                                      String client, String secret, String storageConnectionStr,
                                      String containerName){
        log.info("Getting custom data script ...");
        String customDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource(AZURE_CUSTOM_DATA_CLASSPATH_LOCATION);
            inputStream = resource.getInputStream();
            customDataScript = IOUtils.toString(inputStream, "UTF-8");
            customDataScript = customDataScript.replace(USER, user);
            customDataScript = customDataScript.replace(TAR_FILE_NAME, artifactName);
            customDataScript = customDataScript.replace(DOMAIN, domain);
            customDataScript = customDataScript.replace(SUBSCRIPTION, subscription);
            customDataScript = customDataScript.replace(CLIENT, client);
            customDataScript = customDataScript.replace(SECRET, secret);
            customDataScript = customDataScript.replace(CONTAINER_NAME, containerName);
            customDataScript = customDataScript.replace(STORAGE_CONNECTION_STRING, storageConnectionStr);
        } catch (Exception e){
            throw new RuntimeException("Unable to read custom data", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got custom data script");
        log.debug("\n" + customDataScript);
        return customDataScript;
    }



}
