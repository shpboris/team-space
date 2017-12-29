package org.teamspace.deploy_azure.commons.constants;

/**
 * Created by shpilb on 20/05/2017.
 */
public class DeploymentConstants {
    public static final String AZ_MYSQL_MODE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-az-mysql-instance.json";
    public static final String MYSQL_MODE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-mysql-instance.json";
    public static final String H2_MODE_DEPLOY_TEMPLATE_CLASSPATH_LOCATION = "classpath:templates/azure-network-instance.json";
    public static final String AZURE_CUSTOM_DATA_CLASSPATH_LOCATION = "classpath:scripts/custom_data.sh";
    public static final String AZURE_DB_CUSTOM_DATA_CLASSPATH_LOCATION = "classpath:scripts/db_custom_data.sh";
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
    public static final String DB_CUSTOM_DATA_KEY = "dbCustomData";
    public static final String ENV_TAG_KEY = "envTag";
    public static final String DB_MODE_AZ_MYSQL = "AZ_MYSQL";
    public static final String AZ_MYSQL_SERVER_SUFFIX = "sqlserver";
    public static final String AZ_MYSQL_DB_SUFFIX = "database";
    public static final String AZ_MYSQL_DOMAIN_SUFFIX = ".mysql.database.azure.com";
    public static final String DB_INSTANCE_PRIVATE_IP = "10.0.1.4";
    public static final String DB_INSTANCE_PRIVATE_IP_KEY = "dbInstancePrivateIp";

}
