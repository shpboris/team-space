package org.teamspace.deploy_azure.commons.constants;

/**
 * Created by shpilb on 20/05/2017.
 */
public class DeploymentConstants {
    public static final String AZURE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-template.json";
    public static final String AZURE_CUSTOM_DATA_CLASSPATH_LOCATION = "classpath:scripts/custom_data.sh";
    public static final String USER = "$user$";
    public static final String DOMAIN = "$domain$";
    public static final String SUBSCRIPTION = "$subscription$";
    public static final String CLIENT = "$client$";
    public static final String SECRET = "$secret$";
    public static final String CONTAINER_NAME = "$container-name$";
    public static final String STORAGE_CONNECTION_STRING = "$storage-connection-string$";
    public static final Integer ARM_DEPLOYMENT_MAX_RETRIES = 20;
    public static final String DEPLOYMENT_OUTPUT_PUBLIC_DNS = "publicDns";
    public static final String ADMIN_USERNAME_KEY = "adminUsername";
    public static final String ADMIN_PASSWORD_KEY = "adminPassword";
    public static final String CUSTOM_DATA_KEY = "customData";
    public static final String ENV_TAG_KEY = "envTag";

}
