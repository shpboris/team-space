package org.teamspace.deploy.service.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.aws.client.AwsClientFactory;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.deploy.domain.*;
import org.teamspace.deploy.service.DeployService;
import org.teamspace.instance.domain.*;
import org.teamspace.instance.service.InstanceManager;
import org.teamspace.network.domain.*;
import org.teamspace.network.service.NetworkManager;

import java.io.File;

import static org.teamspace.commons.constants.DeploymentConstants.BUCKET_ENTITY_TYPE;

/**
 * Created by shpilb on 06/05/2017.
 */
@Slf4j
@Service
public class DeployServiceImpl implements DeployService{

    @Autowired
    private AwsClientFactory awsClientFactory;

    @Autowired
    private NetworkManager networkManager;

    @Autowired
    private InstanceManager instanceManager;


    @Value("${artifactsDir}")
    private String artifactsDir;


    //connect to instance like that - ssh -i KeyPair.pem centos@54.149.13.100
    //KeyPair location is C:\Users\shpilb\Desktop\ts-key-pair\KeyPair.pem
    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        initAwsContext(deployRequest.getRegion());
        CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest(deployRequest.getEnvTag());
        CreateNetworkResponse createNetworkResponse = networkManager.createNetwork(createNetworkRequest);
        CreateInstanceRequest createInstanceRequest =
                new CreateInstanceRequest(deployRequest.getEnvTag(),
                        createNetworkResponse.getPublicSubnetId(),
                            createNetworkResponse.getSecurityGroupId(),
                                deployRequest.getArtifactName());
        CreateInstanceResponse createInstanceResponse = instanceManager.createInstance(createInstanceRequest);
        destroyAwsContext();
        return new DeployResponse(createInstanceResponse.getPublicDns());
    }

    @Override
    public void undeploy(UndeployRequest undeployRequest) {
        initAwsContext(undeployRequest.getRegion());
        DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest(undeployRequest.getEnvTag());
        instanceManager.destroyInstance(destroyInstanceRequest);
        DestroyNetworkRequest destroyNetworkRequest = new DestroyNetworkRequest(undeployRequest.getEnvTag());
        networkManager.destroyNetwork(destroyNetworkRequest);
        destroyAwsContext();
    }

    public void uploadArtifact(String artifactName, Regions region, String envTag){
        String bucketName = AwsEntitiesHelperUtil.getEntityName(envTag, BUCKET_ENTITY_TYPE).toLowerCase();
        AmazonS3 s3Client = AwsContext.getS3Client();
        if(!s3Client.doesBucketExist(bucketName)) {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName, region.getName());
            s3Client.createBucket(createBucketRequest);
        }
        File file = new File(artifactsDir + "/" + artifactName);
        s3Client.putObject(new PutObjectRequest(
                bucketName, artifactName, file));
    }

    private void initAwsContext(String region){
        Regions regions = Regions.fromName(region);
        AwsContext.init(regions, awsClientFactory.getEc2Client(regions),
                awsClientFactory.getS3Client(regions), awsClientFactory.getIAMClient(regions));
    }

    private void destroyAwsContext(){
        AwsContext.destroy();
    }

}
