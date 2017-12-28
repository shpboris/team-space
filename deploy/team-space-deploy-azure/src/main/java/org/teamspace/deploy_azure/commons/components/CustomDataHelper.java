package org.teamspace.deploy_azure.commons.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static org.teamspace.deploy_azure.commons.constants.DeploymentConstants.*;
import static org.teamspace.deploy_common.constants.DeployCommonConstants.*;

@Component
@Slf4j
public class CustomDataHelper {

    @Autowired
    private ResourceLoader resourceLoader;

    public String getCustomDataScript(String envTag, String artifactName, String user, String password,
                                      String dbMode, String domain, String subscription,
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
            customDataScript = customDataScript.replace(DB_MODE, dbMode);
            if(dbMode.equals(DB_MODE_AZ_MYSQL)) {
                customDataScript = customDataScript.replace(DB_HOST, getAzMySqlHost(envTag));
                customDataScript = customDataScript.replace(DB_NAME, getAzMySqlDbName(envTag));
                customDataScript = customDataScript.replace(DB_USER, getAzMySqlUser(user, envTag));
                customDataScript = customDataScript.replace(PASSWORD, password);
            }
        } catch (Exception e){
            throw new RuntimeException("Unable to read custom data", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got custom data script");
        log.debug("\n" + customDataScript);
        return customDataScript;
    }

    private String getAzMySqlHost(String envTag){
        return envTag.toLowerCase() + AZ_MYSQL_SERVER_SUFFIX + AZ_MYSQL_DOMAIN_SUFFIX;
    }

    private String getAzMySqlDbName(String envTag){
        return envTag.toLowerCase() + AZ_MYSQL_DB_SUFFIX;
    }

    private String getAzMySqlUser(String user, String envTag){
        return user + "@" + envTag.toLowerCase() + AZ_MYSQL_SERVER_SUFFIX;
    }


}
