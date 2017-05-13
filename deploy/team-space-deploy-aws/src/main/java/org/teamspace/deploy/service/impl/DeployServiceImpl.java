package org.teamspace.deploy.service.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.aws.client.AwsClientFactory;
import org.teamspace.deploy.domain.DeployRequest;
import org.teamspace.deploy.domain.DeployResponse;
import org.teamspace.deploy.service.DeployService;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by shpilb on 06/05/2017.
 */
@Slf4j
@Service
public class DeployServiceImpl implements DeployService{

    public static String TAG_NAME = "Name";
    public static String TAG_VALUE_PREFIX = "Deployer-";
    public static String VPC_TAG_VALUE = "VPC";
    public static String SUBNET_TAG_VALUE = "SUBNET";
    public static String GATEWAY_TAG_VALUE = "GATEWAY";
    public static String ROUTE_TABLE_TAG_VALUE = "ROUTE_TABLE";
    public static String SECURITY_GROUP_TAG_VALUE = "SECURITY_GROUP";
    public static String INSTANCE_TAG_VALUE = "INSTANCE";
    public static final String IMAGE_FILTER_PRODUCT_CODE = "product-code";
    public static final String CENTOS7_PRODUCT_CODE = "aw0evgkw8e5c1q413zgy5pjce";
    public static final String INSTANCE_TYPE = "t2.micro";
    public static final int MAX_RETRIES = 12;
    public static final int WAIT_TIME_MILLISEC = 10000;
    public static final String DEPLOYER_BUCKET_NAME = "deployer-target";

    public static final String TAR_FILE_NAME = "$tarFileName$";
    public static final String REGION_NAME = "$regionName$";
    public static final String BUCKET_NAME = "$bucketName$";

    @Autowired
    private AwsClientFactory awsClientFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${artifactsDir}")
    private String artifactsDir;

    private AmazonEC2 ec2Client;

    private AmazonS3 s3Client;


    @PostConstruct
    private void initClients(){
        ec2Client = awsClientFactory.getEc2Client();
        s3Client = awsClientFactory.getS3Client();
    }



    @Override
    //connect to instance like that - ssh -i KeyPair.pem centos@54.149.13.100
    //KeyPair location is C:\Users\shpilb\Desktop\ts-key-pair\KeyPair.pem
    public DeployResponse deploy(DeployRequest deployRequest) {
        uploadArtifact(deployRequest.getArtifactName());
        Vpc vpc = createVpc("10.0.0.0/16");
        Subnet publicSubnet = createSubnet(vpc, "10.0.0.0/24");
        Subnet privateSubnet = createSubnet(vpc, "10.0.1.0/24");

        InternetGateway internetGateway = createInternetGateway();
        attachGatewayToVpc(vpc, internetGateway);
        RouteTable routeTable = createRouteTable(vpc);
        createRoute(internetGateway, routeTable);
        associateRouteTableWithSubnet(routeTable, publicSubnet);
        mapPublicIpOnLaunch(publicSubnet);

        KeyPair keyPair = createKeyPair("deployerKey");
        String securityGroupId = createSecurityGroup("SSH-SG", vpc);
        authorizeSecurityGroupIngress(securityGroupId, "tcp", 22, "0.0.0.0/0");
        authorizeSecurityGroupIngress(securityGroupId, "tcp", 80, "0.0.0.0/0");
        String amiId = getAmiId(IMAGE_FILTER_PRODUCT_CODE, CENTOS7_PRODUCT_CODE);
        String publicDns = runInstance(amiId, INSTANCE_TYPE, keyPair, securityGroupId, publicSubnet,
                Regions.DEFAULT_REGION.toString(), DEPLOYER_BUCKET_NAME, deployRequest.getArtifactName());
        return new DeployResponse(publicDns);
    }

    public void uploadArtifact(String artifactName){
        if(!s3Client.doesBucketExist(DEPLOYER_BUCKET_NAME)) {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(DEPLOYER_BUCKET_NAME, Region.US_West_2);
            s3Client.createBucket(createBucketRequest);
        }
        File file = new File(artifactsDir + "/" + artifactName);
        s3Client.putObject(new PutObjectRequest(
                DEPLOYER_BUCKET_NAME, artifactName, file));
    }

    public Vpc createVpc(String cidrBlock){
        CreateVpcRequest createVpcRequest = new CreateVpcRequest(cidrBlock);
        CreateVpcResult res = ec2Client.createVpc(createVpcRequest);
        Vpc vpc = res.getVpc();
        createTag(VPC_TAG_VALUE, vpc.getVpcId());
        enableDnsHostnames(vpc);
        return vpc;
    }

    public Subnet createSubnet(Vpc vpc, String cidrBlock){
        CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest(vpc.getVpcId(), cidrBlock);
        CreateSubnetResult createSubnetResult = ec2Client.createSubnet(createSubnetRequest);
        Subnet subnet = createSubnetResult.getSubnet();
        createTag(SUBNET_TAG_VALUE, subnet.getSubnetId());
        return subnet;
    }

    public InternetGateway createInternetGateway(){
        CreateInternetGatewayRequest createInternetGatewayRequest = new CreateInternetGatewayRequest();
        CreateInternetGatewayResult createInternetGatewayResult = ec2Client.createInternetGateway();
        InternetGateway internetGateway = createInternetGatewayResult.getInternetGateway();
        createTag(GATEWAY_TAG_VALUE, internetGateway.getInternetGatewayId());
        return internetGateway;
    }

    public void createTag(String tagValue, String entityId){
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        createTagsRequest.withResources(entityId)
                .withTags(new Tag(TAG_NAME, getTagValue(tagValue)));
        ec2Client.createTags(createTagsRequest);
    }

    public void attachGatewayToVpc(Vpc vpc, InternetGateway internetGateway){
        AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest();
        attachInternetGatewayRequest.withVpcId(vpc.getVpcId()).withInternetGatewayId(internetGateway.getInternetGatewayId());
        AttachInternetGatewayResult attachInternetGatewayResult = ec2Client.attachInternetGateway(attachInternetGatewayRequest);
    }

    public RouteTable createRouteTable(Vpc vpc){
        CreateRouteTableRequest createRouteTableRequest = new CreateRouteTableRequest();
        createRouteTableRequest.withVpcId(vpc.getVpcId());
        CreateRouteTableResult createRouteTableResult = ec2Client.createRouteTable(createRouteTableRequest);
        RouteTable routeTable = createRouteTableResult.getRouteTable();
        createTag(ROUTE_TABLE_TAG_VALUE, routeTable.getRouteTableId());
        return routeTable;
    }

    public void createRoute(InternetGateway internetGateway, RouteTable routeTable){
        CreateRouteRequest createRouteRequest = new CreateRouteRequest();
        createRouteRequest.withGatewayId(internetGateway.getInternetGatewayId())
                .withRouteTableId(routeTable.getRouteTableId())
                    .withDestinationCidrBlock("0.0.0.0/0");
        CreateRouteResult createRouteResult = ec2Client.createRoute(createRouteRequest);
    }

    public void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet){
        AssociateRouteTableRequest associateRouteTableRequest = new AssociateRouteTableRequest();
        associateRouteTableRequest.withRouteTableId(routeTable.getRouteTableId())
                .withSubnetId(subnet.getSubnetId());
        AssociateRouteTableResult associateRouteTableResult = new AssociateRouteTableResult();
        associateRouteTableResult = ec2Client.associateRouteTable(associateRouteTableRequest);
    }

    public void mapPublicIpOnLaunch(Subnet subnet){
        ModifySubnetAttributeRequest modifySubnetAttributeRequest = new ModifySubnetAttributeRequest();
        modifySubnetAttributeRequest.withSubnetId(subnet.getSubnetId());
        modifySubnetAttributeRequest.setMapPublicIpOnLaunch(true);
        ec2Client.modifySubnetAttribute(modifySubnetAttributeRequest);
    }

    public KeyPair createKeyPair(String key){
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
        createKeyPairRequest.withKeyName(key);
        CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
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

    public String createSecurityGroup(String groupName, Vpc vpc){
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
        createSecurityGroupRequest.withVpcId(vpc.getVpcId())
                .withGroupName(groupName).withDescription(groupName + " Description");
        CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);
        String securityGroupId = createSecurityGroupResult.getGroupId();
        createTag(SECURITY_GROUP_TAG_VALUE, securityGroupId);
        return securityGroupId;
    }

    public void authorizeSecurityGroupIngress(String groupId, String protocol, Integer port, String cidr){
        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
        authorizeSecurityGroupIngressRequest.withGroupId(groupId)
                .withIpProtocol(protocol).withFromPort(port).withToPort(port).withCidrIp(cidr);
        ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    public String getAmiId(String imageFilterProductCode, String productCode){
        Filter f = new Filter().withName(imageFilterProductCode).withValues(productCode);
        DescribeImagesRequest request = new DescribeImagesRequest().withFilters(f);
        DescribeImagesResult result = ec2Client.describeImages(request);
        List<Image> images = result.getImages();
        return images.get(0).getImageId();
    }

    public String runInstance(String amiId, String instanceType,
                              KeyPair keyPair, String securityGroupId, Subnet subnet, String regionName,
                                    String bucketName, String tarName){
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(amiId).withInstanceType(instanceType)
                .withKeyName(keyPair.getKeyName())
                .withSecurityGroupIds(securityGroupId)
                .withSubnetId(subnet.getSubnetId())
                .withUserData(getUserDataScript(tarName, regionName, bucketName))
                .withMinCount(1)
                .withMaxCount(1);
        RunInstancesResult runInstancesResult = ec2Client.runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        createTag(INSTANCE_TAG_VALUE, instance.getInstanceId());
        String state = waitForRunningState(instance);
        String publicDns = null;
        if(state.equals("running")){
            publicDns = getInstancePublicDns(instance);
        }
        return publicDns;
    }

    public String waitForRunningState(Instance instance){
        DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
        describeInstanceStatusRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstanceStatusResult describeInstanceStatusResult = ec2Client.describeInstanceStatus(describeInstanceStatusRequest);
        String instanceState = "pending";
        List<InstanceStatus> instancesStatuses = describeInstanceStatusResult.getInstanceStatuses();
        if(instancesStatuses != null && instancesStatuses.size() > 0){
            instanceState = instancesStatuses.get(0).getInstanceState().getName();
        }
        int retriesNum = 0;
        while(!instanceState.equals("running") && retriesNum < MAX_RETRIES){
            retriesNum ++;
            try {
                Thread.sleep(WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for getting instance running state");
            }
            describeInstanceStatusResult = ec2Client.describeInstanceStatus(describeInstanceStatusRequest);
            instancesStatuses = describeInstanceStatusResult.getInstanceStatuses();
            if(instancesStatuses != null && instancesStatuses.size() > 0){
                instanceState = instancesStatuses.get(0).getInstanceState().getName();
            }
        }
        return instanceState;
    }

    public String getInstancePublicDns(Instance instance){
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instance.getInstanceId());
        DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest);
        String publicDns = describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();
        return publicDns;
    }

    private void enableDnsHostnames(Vpc vpc){
        ModifyVpcAttributeRequest modifyVpcAttributeRequest = new ModifyVpcAttributeRequest()
                .withVpcId(vpc.getVpcId())
                .withEnableDnsHostnames(true);

        ec2Client.modifyVpcAttribute(modifyVpcAttributeRequest);

    }

    private String getTagValue(String tagValue){
        return TAG_VALUE_PREFIX + tagValue;
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
}
