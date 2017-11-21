package org.teamspace.enterprise_setup.service.impl;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.cloud_formation.service.StackManagerService;
import org.teamspace.commons.components.UserDataHelper;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.deploy.domain.DeployEnterpriseModeRequest;
import org.teamspace.deploy.domain.DeployResponse;
import org.teamspace.deploy.domain.UndeployEnterpriseModeRequest;
import org.teamspace.enterprise_setup.service.EnterpriseDeployManager;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.teamspace.commons.constants.DeploymentConstants.*;
import static org.teamspace.commons.utils.DbHelperUtil.getDbNormalizedName;

@Service
@Slf4j
public class EnterpriseDeployManagerImpl implements EnterpriseDeployManager {

    public static final String NETWORK_CF_TEMPLATE_CLASSPATH_LOCATION = "classpath:aws-network.template";
    public static final String RDS_CF_TEMPLATE_CLASSPATH_LOCATION = "classpath:aws-rds.template";
    public static final String INSTANCE_CF_TEMPLATE_CLASSPATH_LOCATION = "classpath:aws-instances.template";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StackManagerService stackManagerService;

    @Autowired
    private UserDataHelper userDataHelper;

    @Override
    public DeployResponse createEnvironment(DeployEnterpriseModeRequest deployEnterpriseModeRequest) {

        Stack networkStack = createNetworkStack(deployEnterpriseModeRequest);
        String vpcId = stackManagerService.getStackOutput(networkStack, STACK_OUTPUT_VPC_ID_KEY);
        String publicSubnetId = stackManagerService.getStackOutput(networkStack, STACK_OUTPUT_PUBLIC_SUBNET_ID_KEY);
        String privateSubnetAz1Id = stackManagerService.getStackOutput(networkStack, STACK_OUTPUT_PRIVATE_SUBNET_AZ1_ID_KEY);
        String privateSubnetAz2Id = stackManagerService.getStackOutput(networkStack, STACK_OUTPUT_PRIVATE_SUBNET_AZ2_ID_KEY);

        Stack rdsStack = createRdsStack(deployEnterpriseModeRequest, vpcId, privateSubnetAz1Id, privateSubnetAz2Id);
        String dbPrivateDns = stackManagerService.getStackOutput(rdsStack, STACK_OUTPUT_DB_PRIVATE_DNS_KEY);

        Stack instancesStack = createInstancesStack(deployEnterpriseModeRequest, vpcId,
                publicSubnetId, privateSubnetAz1Id, privateSubnetAz2Id, dbPrivateDns);
        String sitePublicDns = stackManagerService.getStackOutput(instancesStack, STACK_OUTPUT_SITE_PUBLIC_DNS_KEY);


        DeployResponse deployResponse = new DeployResponse(sitePublicDns, null);
        return deployResponse;
    }

    @Override
    public void destroyEnvironment(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
        destroyInstancesStack(undeployEnterpriseModeRequest);
        destroyRdsStack(undeployEnterpriseModeRequest);
        destroyNetworkStack(undeployEnterpriseModeRequest);
    }

    private Stack createNetworkStack(DeployEnterpriseModeRequest deployEnterpriseModeRequest){
        String networkStackName = getStackFullName(deployEnterpriseModeRequest.getEnvTag(),
                NETWORK_STACK_NAME);
        List<Parameter> networkStackParameters = getNetworkStackParameters(deployEnterpriseModeRequest);
        stackManagerService.createStack(networkStackName,
                NETWORK_CF_TEMPLATE_CLASSPATH_LOCATION, networkStackParameters);
        Stack networkStack = stackManagerService.waitForStackCreation(networkStackName,
                ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
        return networkStack;
    }

    private Stack createRdsStack(DeployEnterpriseModeRequest deployEnterpriseModeRequest,
                                 String vpcId, String privateSubnetAz1Id, String privateSubnetAz2Id){
        String rdsStackName = getStackFullName(deployEnterpriseModeRequest.getEnvTag(),
                RDS_STACK_NAME);
        List<Parameter> rdsStackParameters = getRdsStackParameters(deployEnterpriseModeRequest, vpcId,
                privateSubnetAz1Id, privateSubnetAz2Id);
        stackManagerService.createStack(rdsStackName,
                RDS_CF_TEMPLATE_CLASSPATH_LOCATION, rdsStackParameters);
        Stack rdsStack = stackManagerService.waitForStackCreation(rdsStackName,
                ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
        return rdsStack;
    }

    private Stack createInstancesStack(DeployEnterpriseModeRequest deployEnterpriseModeRequest,
                                 String vpcId, String publicSubnetId,
                                       String privateSubnetAz1Id, String privateSubnetAz2Id, String dbPrivateDns){
        String instancesStackName = getStackFullName(deployEnterpriseModeRequest.getEnvTag(),
                INSTANCES_STACK_NAME);
        List<Parameter> parameters = getInstancesStackParameters(deployEnterpriseModeRequest, vpcId,
                publicSubnetId, privateSubnetAz1Id, privateSubnetAz2Id, dbPrivateDns);
        stackManagerService.createStack(instancesStackName, INSTANCE_CF_TEMPLATE_CLASSPATH_LOCATION, parameters);
        Stack instancesStack = stackManagerService.waitForStackCreation(instancesStackName, ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
        return instancesStack;
    }

    private void destroyInstancesStack(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
        String instancesStackName = getStackFullName(undeployEnterpriseModeRequest.getEnvTag(),
                INSTANCES_STACK_NAME);
        stackManagerService.deleteStack(instancesStackName);
        stackManagerService.waitForStackDeletion(instancesStackName, ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
    }

    private void destroyRdsStack(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
        String rdsStackName = getStackFullName(undeployEnterpriseModeRequest.getEnvTag(),
                RDS_STACK_NAME);
        stackManagerService.deleteStack(rdsStackName);
        stackManagerService.waitForStackDeletion(rdsStackName, ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
    }

    private void destroyNetworkStack(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
        String networkStackName = getStackFullName(undeployEnterpriseModeRequest.getEnvTag(),
                NETWORK_STACK_NAME);
        stackManagerService.deleteStack(networkStackName);
        stackManagerService.waitForStackDeletion(networkStackName, ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
    }

    private List<Parameter> getNetworkStackParameters(DeployEnterpriseModeRequest deployEnterpriseModeRequest){
        Parameter envTag = new Parameter()
                .withParameterKey(STACK_PARAMS_ENV_TAG_KEY).withParameterValue(deployEnterpriseModeRequest.getEnvTag());
        return Arrays.asList(envTag);
    }

    private List<Parameter> getRdsStackParameters(DeployEnterpriseModeRequest deployEnterpriseModeRequest,
                                                  String vpcId, String privateSubnetAz1Id, String privateSubnetAz2Id){
        Parameter envTag = new Parameter()
                .withParameterKey(STACK_PARAMS_ENV_TAG_KEY).withParameterValue(deployEnterpriseModeRequest.getEnvTag());
        Parameter vpcIdParam = new Parameter()
                .withParameterKey(STACK_PARAMS_VPC_ID_KEY).withParameterValue(vpcId);
        String subnetsList = String.join(",",privateSubnetAz1Id,
                privateSubnetAz2Id);
        Parameter subnets = new Parameter()
                .withParameterKey(STACK_PARAMS_SUBNETS_KEY).withParameterValue(subnetsList);
        String dbNormalizedName = getDbNormalizedName(deployEnterpriseModeRequest.getArtifactName());
        Parameter dbName = new Parameter().withParameterKey(STACK_PARAMS_DB_NAME_KEY)
                .withParameterValue(dbNormalizedName);
        Parameter dbUsername = new Parameter().withParameterKey(STACK_PARAMS_DB_USERNAME_KEY)
                .withParameterValue(deployEnterpriseModeRequest.getUser());
        Parameter dbPassword = new Parameter().withParameterKey(STACK_PARAMS_DB_PASSWORD_KEY)
                .withParameterValue(deployEnterpriseModeRequest.getPassword());

        return Arrays.asList(envTag, vpcIdParam, subnets, dbName, dbUsername, dbPassword);
    }

    private List<Parameter> getInstancesStackParameters(DeployEnterpriseModeRequest deployEnterpriseModeRequest,
                                                        String vpcId, String publicSubnetId,
                                                        String privateSubnetAz1Id, String privateSubnetAz2Id,
                                                        String dbPrivateDns){
        Parameter vpcIdParam = new Parameter()
                .withParameterKey(STACK_PARAMS_VPC_ID_KEY).withParameterValue(vpcId);
        Parameter publicSubnetIdParam = new Parameter()
                .withParameterKey(STACK_PARAMS_PUBLIC_SUBNET_ID_KEY).withParameterValue(publicSubnetId);
        Parameter privateSubnetAz1IdParam = new Parameter()
                .withParameterKey(STACK_PARAMS_PRIVATE_SUBNET_AZ1_ID_KEY).withParameterValue(privateSubnetAz1Id);
        Parameter privateSubnetAz2IdParam = new Parameter()
                .withParameterKey(STACK_PARAMS_PRIVATE_SUBNET_AZ2_ID_KEY).withParameterValue(privateSubnetAz2Id);
        Parameter envTag = new Parameter()
                .withParameterKey(STACK_PARAMS_ENV_TAG_KEY).withParameterValue(deployEnterpriseModeRequest.getEnvTag());
        Parameter instanceType = new Parameter()
                .withParameterKey(STACK_PARAMS_INSTANCE_TYPE_KEY).withParameterValue(INSTANCE_TYPE);
        Parameter instanceCount = new Parameter()
                .withParameterKey(STACK_PARAMS_INSTANCE_COUNT_KEY).withParameterValue(deployEnterpriseModeRequest.getInstancesCount().toString());
        String bucketName = AwsEntitiesHelperUtil
                .getEntityName(deployEnterpriseModeRequest.getEnvTag(), BUCKET_ENTITY_TYPE).toLowerCase();
        String userDataStr = userDataHelper.getUserDataScript(deployEnterpriseModeRequest.getArtifactName(),
                deployEnterpriseModeRequest.getRegion(), bucketName, DB_MODE_RDS, dbPrivateDns,
                deployEnterpriseModeRequest.getUser(), deployEnterpriseModeRequest.getPassword(), false);
        Parameter userData = new Parameter()
                .withParameterKey(STACK_PARAMS_USER_DATA_KEY).withParameterValue(userDataStr);
        return Arrays.asList(vpcIdParam, publicSubnetIdParam, privateSubnetAz1IdParam, privateSubnetAz2IdParam,
                envTag, instanceType, instanceCount, userData);
    }

    private String getStackFullName(String envTag, String stackName){
        String fullStackName = String.join("",
                envTag, "-", stackName);
        return fullStackName;
    }
}
