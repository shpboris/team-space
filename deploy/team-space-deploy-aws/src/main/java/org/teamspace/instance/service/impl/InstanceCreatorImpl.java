package org.teamspace.instance.service.impl;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.*;
import com.amazonaws.auth.policy.actions.EC2Actions;
import com.amazonaws.auth.policy.actions.S3Actions;
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
import org.teamspace.instance.domain.CreateInstanceRequest;
import org.teamspace.instance.domain.CreateInstanceResponse;
import org.teamspace.instance.service.InstanceCreator;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public CreateInstanceResponse createInstance(CreateInstanceRequest createInstanceRequest) {
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
                createInstanceRequest.getSecurityGroupId(), createInstanceRequest.getSubnetId(),
                AwsContext.getRegion().getName(), bucketName,
                createInstanceRequest.getArtifactName(), createInstanceRequest.getEnvTag());
        waitForApplicationRunningState(publicDns, HTTP_PORT);
        CreateInstanceResponse createInstanceResponse = new CreateInstanceResponse(publicDns);
        return createInstanceResponse;
    }

    private String runInstance(String amiId, String instanceType,
                              KeyPair keyPair, String instanceProfileName, String securityGroupId, String subnetId, String regionName,
                              String bucketName, String tarName, String envTag){
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(amiId).withInstanceType(instanceType)
                .withKeyName(keyPair.getKeyName())
                .withSecurityGroupIds(securityGroupId)
                .withSubnetId(subnetId)
                .withUserData(getUserDataScript(tarName, regionName, bucketName))
                .withIamInstanceProfile(new IamInstanceProfileSpecification().withName(instanceProfileName))
                .withMinCount(1)
                .withBlockDeviceMappings(new BlockDeviceMapping().withDeviceName(BLOCK_DEVICE_NAME)
                        .withEbs(new EbsBlockDevice().withDeleteOnTermination(true)))
                .withMaxCount(1);
        RunInstancesResult runInstancesResult = AwsContext.getEc2Client().runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        tagCreator.createTag(instance.getInstanceId(), INSTANCE_ENTITY_TYPE, envTag);
        String state = waitForInstanceRunningState(instance);
        String publicDns = null;
        if(state.equals(INSTANCE_STATE_RUNNING)){
            publicDns = getInstancePublicDns(instance);
        }
        return publicDns;
    }

    private String waitForInstanceRunningState(Instance instance){
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
        return instanceState;
    }

    private boolean waitForApplicationRunningState(String publicDns, int port){
        boolean isConnected = false;
        int retriesNum = 0;
        while(!isConnected && retriesNum < MAX_RETRIES){
            retriesNum++;
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
            }
        }
        return isConnected;
    }

    private String getInstancePublicDns(Instance instance){
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstancesResult describeInstancesResult = AwsContext.getEc2Client().describeInstances(describeInstancesRequest);
        String publicDns = describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();
        return publicDns;
    }

    private KeyPair createKeyPair(String key){
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
        Filter filter = new Filter().withName(imageFilterProductCode).withValues(productCode);
        DescribeImagesRequest request = new DescribeImagesRequest().withFilters(filter);
        DescribeImagesResult result = AwsContext.getEc2Client().describeImages(request);
        List<Image> images = result.getImages();
        return images.get(images.size() - 1).getImageId();
    }


    private String getUserDataScript(String tarFileName, String regionName, String bucketName){
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:user_data.sh");
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(TAR_FILE_NAME, tarFileName);
            userDataScript = userDataScript.replace(REGION_NAME, regionName);
            userDataScript = userDataScript.replace(BUCKET_NAME, bucketName);
        } catch (Exception e){
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        userDataScript = new String(Base64.encodeBase64(userDataScript.getBytes()));
        return userDataScript;
    }

   private void createInstanceProfile(String name){
        Policy trustPolicy = getTrustPolicy();
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.withRoleName(name).withAssumeRolePolicyDocument(trustPolicy.toJson());
        AwsContext.getIamClient().createRole(createRoleRequest);

        Policy permissionPolicy = getPermissionPolicy();
        CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
        createPolicyRequest.withPolicyName(name).withPolicyDocument(permissionPolicy.toJson());
        CreatePolicyResult createPolicyResult = AwsContext.getIamClient().createPolicy(createPolicyRequest);

        AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest();
        attachRolePolicyRequest.withRoleName(name).withPolicyArn(createPolicyResult.getPolicy().getArn());
        AwsContext.getIamClient().attachRolePolicy(attachRolePolicyRequest);

        CreateInstanceProfileRequest createInstanceProfileRequest = new CreateInstanceProfileRequest();
        createInstanceProfileRequest.withInstanceProfileName(name);
        AwsContext.getIamClient().createInstanceProfile(createInstanceProfileRequest);

        AddRoleToInstanceProfileRequest addRoleToInstanceProfileRequest = new AddRoleToInstanceProfileRequest();
        addRoleToInstanceProfileRequest.withInstanceProfileName(name).withRoleName(name);
        AwsContext.getIamClient().addRoleToInstanceProfile(addRoleToInstanceProfileRequest);

        try {
            Thread.sleep(WAIT_TIME_MILLISEC);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to wait after instance profile creation");
        }
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
