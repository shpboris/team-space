package org.teamspace.instance.service.impl;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.*;
import com.amazonaws.auth.policy.actions.EC2Actions;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.identitymanagement.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.components.TagCreator;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.instance.domain.CreateInstancesRequest;
import org.teamspace.instance.domain.CreateInstancesResponse;
import org.teamspace.instance.service.InstanceCreator;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
@Slf4j
public class InstanceCreatorImpl implements InstanceCreator {

    @Autowired
    private TagCreator tagCreator;

    @Autowired
    private ResourceLoader resourceLoader;


    @Override
    public CreateInstancesResponse createInstances(CreateInstancesRequest createInstanceRequest) {
        log.info("Started instances creation");
        CreateInstancesResponse createDbInstanceResponse = null;
        if(createInstanceRequest.getDbMode().equals(DB_MODE_MYSQL)) {
            createDbInstanceResponse = createMySqlDbInstance(createInstanceRequest);
        } else if(createInstanceRequest.getDbMode().equals(DB_MODE_RDS)){
            createDbInstanceResponse = createRdsDbInstance(createInstanceRequest);
        }
        CreateInstancesResponse createAppInstanceResponse = createAppInstance(createInstanceRequest);
        if(createDbInstanceResponse != null) {
            String dbInstancePrivateDns = createDbInstanceResponse.getDbInstancePrivateDns();
            createAppInstanceResponse.setDbInstancePrivateDns(dbInstancePrivateDns);
        }
        log.info("Completed instances creation");
        return createAppInstanceResponse;
    }


    private CreateInstancesResponse createAppInstance(CreateInstancesRequest createInstanceRequest) {
        log.info("Started app instance creation");
        String keyPairName = AwsEntitiesHelperUtil.
                getEntityName(createInstanceRequest.getEnvTag(), KEY_PAIR_ENTITY_TYPE);
        KeyPair keyPair = createKeyPair(keyPairName);

        String profileName = AwsEntitiesHelperUtil
                .getEntityName(createInstanceRequest.getEnvTag(), PROFILE_AND_ROLE_ENTITY_TYPE);
        createInstanceProfile(profileName);

        String bucketName = AwsEntitiesHelperUtil
                .getEntityName(createInstanceRequest.getEnvTag(), BUCKET_ENTITY_TYPE).toLowerCase();

        String amiId = getAmiId(IMAGE_FILTER_PRODUCT_CODE, CENTOS7_PRODUCT_CODE);
        String publicDns = runInstance(amiId, INSTANCE_TYPE, keyPair, profileName,
                createInstanceRequest.getSecurityGroupId(), createInstanceRequest.getPublicSubnetId(),
                AwsContext.getRegion().getName(), bucketName,
                createInstanceRequest.getArtifactName(), createInstanceRequest.getEnvTag(),
                createInstanceRequest.getDbMode(),
                createInstanceRequest.getDbInstancePrivateDns(),
                createInstanceRequest.getUser(), createInstanceRequest.getPassword());
        waitForApplicationRunningState(publicDns, HTTP_PORT);
        CreateInstancesResponse createInstanceResponse = new CreateInstancesResponse();
        createInstanceResponse.setAppInstancePublicDns(publicDns);
        log.info("Completed app instance creation");
        return createInstanceResponse;
    }

    private CreateInstancesResponse createRdsDbInstance(CreateInstancesRequest createInstanceRequest) {
        log.info("Started RDS instance creation");
        CreateInstancesResponse createInstanceResponse = new CreateInstancesResponse();
        try {
            String stackName = createStack(createInstanceRequest);
            Stack stack = waitForStackCreation(stackName);
            if (!stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString())) {
                log.error("Stack creation failed with stack status {} and reason {}",
                        stack.getStackStatus(), stack.getStackName());
                throw new RuntimeException("Stack creation failed");
            }
            String privateDns = getDbPrivateDnsFromStackOutput(stackName);
            log.debug("RDS instance private DNS is {}", privateDns);
            createInstanceRequest.setDbInstancePrivateDns(privateDns);

            createInstanceResponse.setDbInstancePrivateDns(privateDns);
        } catch (Exception e){
            throw new RuntimeException("Stack creation failed", e);
        }
        log.info("Completed RDS instance creation");
        return createInstanceResponse;
    }

    private String createStack(CreateInstancesRequest createInstanceRequest) throws Exception{
        log.info("Started stack creation");
        CreateStackRequest createStackRequest = new CreateStackRequest();
        String stackName = createInstanceRequest.getEnvTag() + "-" + "STACK";
        log.debug("Stack names is {}", stackName);
        createStackRequest.setStackName(stackName);
        createStackRequest.setParameters(getStackParameters(createInstanceRequest));
        Resource resource = resourceLoader.getResource("classpath:aws-rds.template");
        InputStream inputStream = resource.getInputStream();
        String rdsTemplate = IOUtils.toString(inputStream, "UTF-8");
        createStackRequest.setTemplateBody(rdsTemplate);
        AwsContext.getCloudFormationClient().createStack(createStackRequest);
        log.info("Completed stack creation");
        return stackName;
    }

    public Stack waitForStackCreation(String stackName) throws Exception {
        log.info("Started waiting for stack creation completion");
        Stack stack = null;
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        describeStacksRequest.setStackName(stackName);
        int retriesCount = 0;
        boolean isStackCreationCompleted = false;
        while (!isStackCreationCompleted && retriesCount < RDS_CF_MAX_RETRIES) {
            retriesCount++;
            Thread.sleep(RDS_CF_WAIT_TIME_MILLISEC);
            stack = AwsContext.getCloudFormationClient().
                    describeStacks(describeStacksRequest).getStacks().get(0);
            if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                    stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                    stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                    stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
                isStackCreationCompleted = true;
            }
            log.debug("Waiting for stack creation: attempt #{}, stack status is {}", retriesCount, stack.getStackStatus());
        }
        log.info("Finished waiting for stack creation completion, stack status is {}", stack.getStackStatus());
        return stack;
    }

    String getDbPrivateDnsFromStackOutput(String stackName){
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
        DescribeStacksResult describeStacksResult = AwsContext.getCloudFormationClient().describeStacks(describeStacksRequest);
        List<Output> stackOutputs = describeStacksResult.getStacks().get(0).getOutputs();
        return stackOutputs.stream()
                .filter(output -> output.getOutputKey().equals("DBPrivateDns")).findFirst().get().getOutputValue();
    }

    private List<Parameter> getStackParameters(CreateInstancesRequest createInstanceRequest){
        String subnetsList = String.join(",",createInstanceRequest.getPrivateSubnetIdFirstAz(),
                createInstanceRequest.getPrivateSubnetIdSecondAz());
        Parameter subnets = new Parameter()
                .withParameterKey(STACK_PARAMS_SUBNETS_KEY).withParameterValue(subnetsList);
        Parameter securityGroup = new Parameter()
                .withParameterKey(STACK_PARAMS_DB_SECURITY_GROUP_KEY).withParameterValue(createInstanceRequest.getSecurityGroupId());
        String dbNormalizedName = getDbNormalizedName(createInstanceRequest.getArtifactName());
        Parameter dbName = new Parameter().withParameterKey(STACK_PARAMS_DB_NAME_KEY)
                .withParameterValue(dbNormalizedName);
        Parameter dbUsername = new Parameter().withParameterKey(STACK_PARAMS_DB_USERNAME_KEY)
                .withParameterValue(createInstanceRequest.getUser());
        Parameter dbPassword = new Parameter().withParameterKey(STACK_PARAMS_DB_PASSWORD_KEY)
                .withParameterValue(createInstanceRequest.getPassword());

        return Arrays.asList(subnets, securityGroup, dbName, dbUsername, dbPassword);
    }

    private CreateInstancesResponse createMySqlDbInstance(CreateInstancesRequest createInstanceRequest) {
        log.info("Started DB instance creation");
        String keyPairName = AwsEntitiesHelperUtil.
                getEntityName(createInstanceRequest.getEnvTag(), DB_KEY_PAIR_ENTITY_TYPE);
        KeyPair keyPair = createKeyPair(keyPairName);

        String amiId = getAmiId(IMAGE_FILTER_PRODUCT_CODE, CENTOS7_PRODUCT_CODE);
        String privateDns = runDbInstance(amiId, INSTANCE_TYPE, keyPair,
                createInstanceRequest.getSecurityGroupId(), createInstanceRequest.getPrivateSubnetIdFirstAz(),
                createInstanceRequest.getEnvTag(), createInstanceRequest.getArtifactName(), createInstanceRequest.getUser(),
                createInstanceRequest.getPassword());
        createInstanceRequest.setDbInstancePrivateDns(privateDns);
        CreateInstancesResponse createInstanceResponse = new CreateInstancesResponse();
        createInstanceResponse.setDbInstancePrivateDns(privateDns);
        log.info("Completed DB instance creation");
        return createInstanceResponse;
    }

    private String runInstance(String amiId, String instanceType,
                              KeyPair keyPair, String instanceProfileName, String securityGroupId, String subnetId, String regionName,
                              String bucketName, String tarName, String envTag, String dbMode, String dbInstancePrivateDns, String user, String password){
        log.info("Running instance ...");
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(amiId).withInstanceType(instanceType)
                .withKeyName(keyPair.getKeyName())
                .withSecurityGroupIds(securityGroupId)
                .withSubnetId(subnetId)
                .withUserData(getUserDataScript(tarName, regionName, bucketName, dbMode, dbInstancePrivateDns, user, password))
                .withIamInstanceProfile(new IamInstanceProfileSpecification().withName(instanceProfileName))
                .withMinCount(1)
                .withBlockDeviceMappings(new BlockDeviceMapping().withDeviceName(BLOCK_DEVICE_NAME)
                        .withEbs(new EbsBlockDevice().withDeleteOnTermination(true)))
                .withMaxCount(1);
        RunInstancesResult runInstancesResult = AwsContext.getEc2Client().runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        tagCreator.createTag(instance.getInstanceId(), APP_INSTANCE_ENTITY_TYPE, envTag);
        String state = waitForInstanceRunningState(instance);
        String publicDns = null;
        if(state.equals(INSTANCE_STATE_RUNNING)){
            publicDns = getInstancePublicDns(instance);
        }
        log.info("Instance is running with public DNS: " + publicDns);
        return publicDns;
    }


    private String runDbInstance(String amiId, String instanceType,
                               KeyPair keyPair, String securityGroupId, String subnetId,
                                 String envTag, String tarFileName, String user, String password){
        log.info("Running DB instance ...");
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(amiId).withInstanceType(instanceType)
                .withKeyName(keyPair.getKeyName())
                .withSecurityGroupIds(securityGroupId)
                .withSubnetId(subnetId)
                .withUserData(getDbUserDataScript(tarFileName, user, password))
                .withMinCount(1)
                .withBlockDeviceMappings(new BlockDeviceMapping().withDeviceName(BLOCK_DEVICE_NAME)
                        .withEbs(new EbsBlockDevice().withDeleteOnTermination(true)))
                .withMaxCount(1);
        RunInstancesResult runInstancesResult = AwsContext.getEc2Client().runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        tagCreator.createTag(instance.getInstanceId(), DB_INSTANCE_ENTITY_TYPE, envTag);
        String state = waitForInstanceRunningState(instance);
        String privateDns = null;
        if(state.equals(INSTANCE_STATE_RUNNING)){
            privateDns = getInstancePrivateDns(instance);
        }
        log.info("DB instance is running with private DNS: " + privateDns);
        return privateDns;
    }

    private String waitForInstanceRunningState(Instance instance){
        log.info("Waiting for instance running state ...");
        DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
        describeInstanceStatusRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstanceStatusResult describeInstanceStatusResult = AwsContext.getEc2Client().describeInstanceStatus(describeInstanceStatusRequest);
        String instanceState = INSTANCE_STATE_PENDING;
        List<InstanceStatus> instancesStatuses = describeInstanceStatusResult.getInstanceStatuses();
        if(instancesStatuses != null && instancesStatuses.size() > 0){
            instanceState = instancesStatuses.get(0).getInstanceState().getName();
        }
        int retriesNum = 0;
        while(!instanceState.equals(INSTANCE_STATE_RUNNING) && retriesNum < MAX_RETRIES){
            retriesNum ++;
            try {
                Thread.sleep(WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for getting instance running state");
            }
            describeInstanceStatusResult = AwsContext.getEc2Client().describeInstanceStatus(describeInstanceStatusRequest);
            instancesStatuses = describeInstanceStatusResult.getInstanceStatuses();
            if(instancesStatuses != null && instancesStatuses.size() > 0){
                instanceState = instancesStatuses.get(0).getInstanceState().getName();
            }
        }
        log.info("Waiting for instance running state is over, instance state is: " + instanceState);
        return instanceState;
    }

    private boolean waitForApplicationRunningState(String publicDns, int port){
        log.info("Waiting for application running state ...");
        boolean isConnected = false;
        int retryNum = 0;
        while(!isConnected && retryNum < MAX_RETRIES){
            retryNum++;
            try {
                Thread.sleep(WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for application connected state");
            }
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(publicDns, port), TIMEOUT_MILLISEC);
                isConnected = true;
            } catch (Exception e) {
                log.debug("Attempt #" + retryNum + " to connect to application failed");
            }
        }
        log.info("Wait for application running state is over, state is: " + isConnected);
        return isConnected;
    }

    private String getInstancePublicDns(Instance instance){
        log.info("Getting instance public DNS ...");
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstancesResult describeInstancesResult = AwsContext.getEc2Client().describeInstances(describeInstancesRequest);
        String publicDns = describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();
        log.info("Got instance public DNS: " + publicDns);
        return publicDns;
    }

    private String getInstancePrivateDns(Instance instance){
        log.info("Getting instance private DNS ...");
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstancesResult describeInstancesResult = AwsContext.getEc2Client().describeInstances(describeInstancesRequest);
        String privateDns = describeInstancesResult.getReservations().get(0).getInstances().get(0).getPrivateDnsName();
        log.info("Got instance private DNS: " + privateDns);
        return privateDns;
    }

    private KeyPair createKeyPair(String key){
        log.info("Creating key pair ...");
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
        createKeyPairRequest.withKeyName(key);
        CreateKeyPairResult createKeyPairResult = AwsContext.getEc2Client().createKeyPair(createKeyPairRequest);
        KeyPair keyPair = createKeyPairResult.getKeyPair();
        String keyPairFilePath = System.getProperty("user.dir") + "/KeyPair.pem";
        try {
            Files.write(Paths.get(keyPairFilePath), createKeyPairResult.getKeyPair().getKeyMaterial().getBytes());
            log.info("Key Pair file can be found under:\n" + keyPairFilePath);
        } catch (IOException e) {
            log.warn("Unable to write keyPair file to path:\n" + keyPairFilePath, e);
        }
        return keyPair;
    }

    private String getAmiId(String imageFilterProductCode, String productCode){
        log.info("Getting AMI id");
        Filter filter = new Filter().withName(imageFilterProductCode).withValues(productCode);
        DescribeImagesRequest request = new DescribeImagesRequest().withFilters(filter);
        DescribeImagesResult result = AwsContext.getEc2Client().describeImages(request);
        List<Image> images = result.getImages();
        String amiId = images.get(images.size() - 1).getImageId();
        log.info("AMI id: " + amiId);
        return amiId;
    }


    private String getUserDataScript(String tarFileName, String regionName, String bucketName, String dbMode, String dbInstancePrivateDns, String user, String password){
        log.info("Getting user data script ...");
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:user_data.sh");
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(TAR_FILE_NAME, tarFileName);
            userDataScript = userDataScript.replace(REGION_NAME, regionName);
            userDataScript = userDataScript.replace(BUCKET_NAME, bucketName);
            userDataScript = userDataScript.replace(USER, user);
            userDataScript = userDataScript.replace(PASSWORD, password);
            userDataScript = userDataScript.replace(DB_MODE, dbMode);
            if(dbMode.equals(DB_MODE_MYSQL) || dbMode.equals(DB_MODE_RDS)) {
                userDataScript = userDataScript.replace(DB_HOST, dbInstancePrivateDns);
                String dbUrl = getDbUrl(dbInstancePrivateDns, tarFileName);
                userDataScript = userDataScript.replace(DB_URL, dbUrl);
            }
        } catch (Exception e){
            log.error("Unable to read user data", e);
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got user data script");
        log.debug("\n" + userDataScript);
        userDataScript = new String(Base64.encodeBase64(userDataScript.getBytes()));
        return userDataScript;
    }

    private String getDbUserDataScript(String tarFileName, String user, String password){
        log.info("Getting DB user data script ...");
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:db_user_data.sh");
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(USER, user);
            userDataScript = userDataScript.replace(PASSWORD, password);
            userDataScript = userDataScript.replace(DB_NAME, getDbNormalizedName(tarFileName));
        } catch (Exception e){
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got DB user data script");
        log.debug("\n" + userDataScript);
        userDataScript = new String(Base64.encodeBase64(userDataScript.getBytes()));
        return userDataScript;
    }

    private String getDbUrl(String dbInstancePrivateDns, String tarFileName){
        String dbUrl = DB_URL_TEMPLATE.replace(DB_HOST, dbInstancePrivateDns);
        String dbName = getDbNormalizedName(tarFileName);
        dbUrl = dbUrl.replace(DB_NAME, dbName);
        return dbUrl;
    }

    private String getDbNormalizedName(String tarFileName){
        String dbName = tarFileName.replaceAll("[^a-zA-Z0-9]+","");
        return  dbName;
    }

   private void createInstanceProfile(String name){
        log.info("Creating instance profile ...");
        Policy trustPolicy = getTrustPolicy();
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.withRoleName(name).withAssumeRolePolicyDocument(trustPolicy.toJson());
        AwsContext.getIamClient().createRole(createRoleRequest);
        log.info("Created role: " + name);

        Policy permissionPolicy = getPermissionPolicy();
        CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
        createPolicyRequest.withPolicyName(name).withPolicyDocument(permissionPolicy.toJson());
        CreatePolicyResult createPolicyResult = AwsContext.getIamClient().createPolicy(createPolicyRequest);
        log.info("Created policy: " + createPolicyResult.getPolicy().getArn());

        AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest();
        attachRolePolicyRequest.withRoleName(name).withPolicyArn(createPolicyResult.getPolicy().getArn());
        AwsContext.getIamClient().attachRolePolicy(attachRolePolicyRequest);
        log.info("Attached role to policy");

        CreateInstanceProfileRequest createInstanceProfileRequest = new CreateInstanceProfileRequest();
        createInstanceProfileRequest.withInstanceProfileName(name);
        AwsContext.getIamClient().createInstanceProfile(createInstanceProfileRequest);
        log.info("Created instance profile");

        AddRoleToInstanceProfileRequest addRoleToInstanceProfileRequest = new AddRoleToInstanceProfileRequest();
        addRoleToInstanceProfileRequest.withInstanceProfileName(name).withRoleName(name);
        AwsContext.getIamClient().addRoleToInstanceProfile(addRoleToInstanceProfileRequest);
        log.info("Added role to instance profile");

        try {
            Thread.sleep(WAIT_TIME_MILLISEC);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to wait after instance profile creation");
        }
        log.info("Instance profile finally created");
    }


    private Policy getTrustPolicy(){
        Principal principal = new Principal(Principal.Services.AmazonEC2);
        com.amazonaws.auth.policy.Statement statement = new com.amazonaws.auth.policy.Statement(com.amazonaws.auth.policy.Statement.Effect.Allow)
                .withActions(STSActions.AssumeRole)
                .withPrincipals(principal);
        Policy policy = new Policy()
                .withStatements(statement);
        return policy;
    }

    private Policy getPermissionPolicy(){
        com.amazonaws.auth.policy.Statement s3Statement = new com.amazonaws.auth.policy.Statement(com.amazonaws.auth.policy.Statement.Effect.Allow)
                .withResources(new com.amazonaws.auth.policy.Resource("*"))
                .withActions(S3Actions.AllS3Actions);

        com.amazonaws.auth.policy.Statement ec2Statement = new com.amazonaws.auth.policy.Statement(com.amazonaws.auth.policy.Statement.Effect.Allow)
                .withResources(new com.amazonaws.auth.policy.Resource("*"))
                .withActions(EC2Actions.AllEC2Actions);

        Policy policy = new Policy();
        policy.withStatements(s3Statement, ec2Statement);
        return policy;
    }

}
