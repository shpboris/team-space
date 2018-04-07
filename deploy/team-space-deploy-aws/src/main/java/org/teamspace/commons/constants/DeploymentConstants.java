package org.teamspace.commons.constants;

import com.amazonaws.regions.Regions;

/**
 * Created by shpilb on 20/05/2017.
 */
public class DeploymentConstants {
    public static final String TAG_NAME = "Name";
    public static final String VPC_ENTITY_TYPE = "VPC";
    public static final String PUBLIC_SUBNET_ENTITY_TYPE = "PUBLIC_SUBNET";
    public static final String PRIVATE_SUBNET_FIRST_AZ_ENTITY_TYPE = "PRIVATE_SUBNET_AZ1";
    public static final String PRIVATE_SUBNET_SECOND_AZ_ENTITY_TYPE = "PRIVATE_SUBNET_AZ2";
    public static final String GATEWAY_ENTITY_TYPE = "GATEWAY";
    public static final String PUBLIC_ROUTE_TABLE_ENTITY_TYPE = "PUBLIC_ROUTE_TABLE";
    public static final String PRIVATE_ROUTE_TABLE_ENTITY_TYPE = "PRIVATE_ROUTE_TABLE";
    public static final String SECURITY_GROUP_ENTITY_TYPE = "SECURITY_GROUP";
    public static final String APP_INSTANCE_ENTITY_TYPE = "APP_INSTANCE";
    public static final String DB_INSTANCE_ENTITY_TYPE = "DB_INSTANCE";
    public static final String KEY_PAIR_ENTITY_TYPE = "KEY_PAIR";
    public static final String DB_KEY_PAIR_ENTITY_TYPE = "DB_KEY_PAIR";
    public static final String PROFILE_AND_ROLE_ENTITY_TYPE = "PROFILE_ROLE";
    public static final String BUCKET_ENTITY_TYPE = "BUCKET";
    public static final String RDS_STACK_ENTITY_TYPE = "RDS-STACK";

    public static final String IMAGE_FILTER_PRODUCT_CODE = "product-code";
    public static final String CENTOS7_PRODUCT_CODE = "aw0evgkw8e5c1q413zgy5pjce";
    public static final String INSTANCE_TYPE = "t2.micro";
    public static final int MAX_RETRIES = 12;
    public static final int WAIT_TIME_MILLISEC = 10000;
    public static final int AUTO_SCALE_MAX_RETRIES = 20;
    public static final int AUTO_SCALE_WAIT_TIME_MILLISEC = 60000;
    public static final int RDS_CF_MAX_RETRIES = 30;
    public static final int ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES = 30;
    public static final int CF_STACK_CREATION_WAIT_TIME_MILLISEC = 60000;
    public static final int TIMEOUT_MILLISEC = 30000;
    public static final String INSTANCE_STATE_RUNNING = "running";
    public static final String INSTANCE_STATE_PENDING = "pending";
    public static final String INSTANCE_STATE_TERMINATED = "terminated";
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final int MYSQL_PORT = 3306;
    public static final int SSH_PORT = 22;
    public static final String BLOCK_DEVICE_NAME = "/dev/sda1";

    public static final String REGION_NAME = "$regionName$";
    public static final String BUCKET_NAME = "$bucketName$";
    public static final String USER = "$user$";
    public static final String DB_MODE_RDS = "RDS";
    public static final String DB_URL = "$dburl$";
    public static final String DB_URL_TEMPLATE = "jdbc:mysql://$dbhost$:3306/$dbname$";
    public static final String DEFAULT_REGION_NAME = Regions.DEFAULT_REGION.toString();

    public static final String NETWORK_RDS_INSTANCES_STACK_NAME = "NETWORK-RDS-INSTANCES-STACK";
    public static final String NETWORK_STACK_NAME = "NETWORK-STACK";
    public static final String RDS_STACK_NAME = "RDS-STACK";
    public static final String INSTANCES_STACK_NAME = "INSTANCES-STACK";

    public static final String STACK_PARAMS_SUBNETS_KEY = "Subnets";
    public static final String STACK_PARAMS_DB_SECURITY_GROUP_KEY = "DBSecurityGroup";
    public static final String STACK_PARAMS_DB_NAME_KEY = "DBName";
    public static final String STACK_PARAMS_DB_USERNAME_KEY = "DBUsername";
    public static final String STACK_PARAMS_DB_PASSWORD_KEY = "DBPassword";
    public static final String STACK_PARAMS_DB_CLASS_KEY = "DBClass";
    public static final String STACK_PARAMS_DB_ALLOCATED_STORAGE_KEY = "DBAllocatedStorage";

    public static final String STACK_PARAMS_ENV_TAG_KEY = "EnvTag";
    public static final String STACK_PARAMS_INSTANCE_TYPE_KEY = "InstanceType";
    public static final String STACK_PARAMS_INSTANCE_COUNT_KEY = "InstanceCount";
    public static final String STACK_PARAMS_USER_DATA_KEY = "UserData";
    public static final String STACK_PARAMS_VPC_ID_KEY = "VpcId";
    public static final String STACK_PARAMS_PUBLIC_SUBNET_ID_KEY = "PublicSubnetId";
    public static final String STACK_PARAMS_PRIVATE_SUBNET_AZ1_ID_KEY = "PrivateSubnetAz1Id";
    public static final String STACK_PARAMS_PRIVATE_SUBNET_AZ2_ID_KEY = "PrivateSubnetAz2Id";

    public static final String STACK_OUTPUT_SITE_PUBLIC_DNS_KEY = "SitePublicDNS";
    public static final String STACK_OUTPUT_VPC_ID_KEY = "VpcId";
    public static final String STACK_OUTPUT_PUBLIC_SUBNET_ID_KEY = "PublicSubnetId";
    public static final String STACK_OUTPUT_PRIVATE_SUBNET_AZ1_ID_KEY = "PrivateSubnetAz1Id";
    public static final String STACK_OUTPUT_PRIVATE_SUBNET_AZ2_ID_KEY = "PrivateSubnetAz2Id";
    public static final String STACK_OUTPUT_DB_PRIVATE_DNS_KEY = "DBPrivateDns";

    public static final String USER_DATA_CLASSPATH_LOCATION = "classpath:user_data.sh";
    public static final String INITIAL_USER_DATA_CLASSPATH_LOCATION = "classpath:initial_user_data.sh";

}
