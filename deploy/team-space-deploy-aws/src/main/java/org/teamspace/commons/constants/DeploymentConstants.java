package org.teamspace.commons.constants;

import com.amazonaws.regions.Regions;

/**
 * Created by shpilb on 20/05/2017.
 */
public class DeploymentConstants {
    public static final String TAG_NAME = "Name";
    public static final String VPC_ENTITY_TYPE = "VPC";
    public static final String SUBNET_ENTITY_TYPE = "SUBNET";
    public static final String GATEWAY_ENTITY_TYPE = "GATEWAY";
    public static final String ROUTE_TABLE_ENTITY_TYPE = "ROUTE_TABLE";
    public static final String SECURITY_GROUP_ENTITY_TYPE = "SECURITY_GROUP";
    public static final String INSTANCE_ENTITY_TYPE = "INSTANCE";
    public static final String KEY_PAIR_ENTITY_TYPE = "KEY_PAIR";
    public static final String DB_KEY_PAIR_ENTITY_TYPE = "DB_KEY_PAIR";
    public static final String PROFILE_AND_ROLE_ENTITY_TYPE = "PROFILE_ROLE";
    public static final String BUCKET_ENTITY_TYPE = "BUCKET";

    public static final String IMAGE_FILTER_PRODUCT_CODE = "product-code";
    public static final String CENTOS7_PRODUCT_CODE = "aw0evgkw8e5c1q413zgy5pjce";
    public static final String INSTANCE_TYPE = "t2.micro";
    public static final int MAX_RETRIES = 12;
    public static final int WAIT_TIME_MILLISEC = 10000;
    public static final int TIMEOUT_MILLISEC = 30000;
    public static final String INSTANCE_STATE_RUNNING = "running";
    public static final String INSTANCE_STATE_PENDING = "pending";
    public static final String INSTANCE_STATE_TERMINATED = "terminated";
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final int SSH_PORT = 22;
    public static final String BLOCK_DEVICE_NAME = "/dev/sda1";

    public static final String TAR_FILE_NAME = "$tarFileName$";
    public static final String REGION_NAME = "$regionName$";
    public static final String BUCKET_NAME = "$bucketName$";
    public static final String USER = "$user$";
    public static final String PASSWORD = "$pass$";
    public static final String DEFAULT_REGION_NAME = Regions.DEFAULT_REGION.toString();
    public static final boolean OVERRIDE_EXISTING_ARTIFACT = false;
    public static final String ARTIFACT_EXTENSION = ".tar.gz";
}
