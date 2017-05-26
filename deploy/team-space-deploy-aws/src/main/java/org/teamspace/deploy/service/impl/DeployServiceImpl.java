package org.teamspace.deploy.service.impl;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.aws.client.AwsClientFactory;
import org.teamspace.deploy.domain.*;
import org.teamspace.deploy.service.DeployService;
import org.teamspace.instance.domain.*;
import org.teamspace.instance.service.InstanceManager;
import org.teamspace.network.domain.*;
import org.teamspace.network.service.NetworkManager;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by shpilb on 06/05/2017.
 */
@Slf4j
@Service
public class DeployServiceImpl implements DeployService{

    public static final String DEPLOYER_BUCKET_NAME = "deployer-target";

    @Autowired
    private AwsClientFactory awsClientFactory;

    @Autowired
    private NetworkManager networkManager;

    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${artifactsDir}")
    private String artifactsDir;

    private AmazonEC2 ec2Client;

    private AmazonS3 s3Client;

    private AmazonIdentityManagement iamClient;


    @PostConstruct
    private void initClients(){
        ec2Client = awsClientFactory.getEc2Client();
        s3Client = awsClientFactory.getS3Client();
        iamClient = awsClientFactory.getIAMClient();
    }

    //connect to instance like that - ssh -i KeyPair.pem centos@54.149.13.100
    //KeyPair location is C:\Users\shpilb\Desktop\ts-key-pair\KeyPair.pem
    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest(deployRequest.getEnvTag());
        CreateNetworkResponse createNetworkResponse = networkManager.createNetwork(createNetworkRequest);
        CreateInstanceRequest createInstanceRequest =
                new CreateInstanceRequest(deployRequest.getEnvTag(),
                        createNetworkResponse.getPublicSubnetId(),
                            createNetworkResponse.getSecurityGroupId(),
                                deployRequest.getArtifactName());
        CreateInstanceResponse createInstanceResponse = instanceManager.createInstance(createInstanceRequest);
        return new DeployResponse(createInstanceResponse.getPublicDns());
    }

    @Override
    public void undeploy(UndeployRequest undeployRequest) {
        DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest(undeployRequest.getEnvTag());
        instanceManager.destroyInstance(destroyInstanceRequest);
        DestroyNetworkRequest destroyNetworkRequest = new DestroyNetworkRequest(undeployRequest.getEnvTag());
        networkManager.destroyNetwork(destroyNetworkRequest);
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

}
