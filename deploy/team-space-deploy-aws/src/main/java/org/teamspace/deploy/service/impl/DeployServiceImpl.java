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
import org.teamspace.deploy.service.DeployService;
import org.teamspace.deploy_common.domain.*;
import org.teamspace.enterprise_setup.service.EnterpriseDeployManager;
import org.teamspace.instance.domain.*;
import org.teamspace.instance.service.InstanceManager;
import org.teamspace.network.domain.*;
import org.teamspace.network.service.NetworkManager;

import java.io.File;

import static org.teamspace.commons.constants.DeploymentConstants.ARTIFACT_EXTENSION;
import static org.teamspace.commons.constants.DeploymentConstants.BUCKET_ENTITY_TYPE;
import static org.teamspace.commons.constants.DeploymentConstants.OVERRIDE_EXISTING_ARTIFACT;

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

    @Autowired
    private EnterpriseDeployManager enterpriseDeployManager;


    @Value("${artifactsDir}")
    private String artifactsDir;


    @Override
    public DeployResponse deploy(DeployEnterpriseModeRequest deployEnterpriseModeRequest) {
        log.info("Started enterprise mode deploy to region: {}, env tag: {}, instances count: {}",
                deployEnterpriseModeRequest.getRegion(),
                    deployEnterpriseModeRequest.getEnvTag(), deployEnterpriseModeRequest.getInstancesCount());
        initAwsContext(deployEnterpriseModeRequest.getRegion());
        uploadArtifact(deployEnterpriseModeRequest.getArtifactName(), deployEnterpriseModeRequest.getEnvTag());
        DeployResponse deployResponse = enterpriseDeployManager.createEnvironment(deployEnterpriseModeRequest);
        destroyAwsContext();
        log.info("Completed enterprise mode deploy to region: {}, env tag: {}, instances count: {}",
                deployEnterpriseModeRequest.getRegion(),
                deployEnterpriseModeRequest.getEnvTag(), deployEnterpriseModeRequest.getInstancesCount());

        return deployResponse;
    }

    //connect to instance like that - ssh -i KeyPair.pem centos@54.149.13.100
    //KeyPair location is C:\Users\shpilb\Desktop\ts-key-pair\KeyPair.pem
    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        log.info("Started deploy to region: " + deployRequest
                .getRegion() + " , env tag: " + deployRequest.getEnvTag());
        initAwsContext(deployRequest.getRegion());
        uploadArtifact(deployRequest.getArtifactName(), deployRequest.getEnvTag());
        CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest(deployRequest.getEnvTag(),
                deployRequest.getDbMode());
        CreateNetworkResponse createNetworkResponse = networkManager.createNetwork(createNetworkRequest);
        CreateInstancesRequest createInstanceRequest =
                new CreateInstancesRequest(deployRequest.getEnvTag(),
                        createNetworkResponse.getVpcId(),
                        createNetworkResponse.getPublicSubnetId(),
                        createNetworkResponse.getPrivateSubnetIdFirstAz(),
                        createNetworkResponse.getPrivateSubnetIdSecondAz(),
                            createNetworkResponse.getSecurityGroupId(),
                                deployRequest.getArtifactName(),deployRequest.getDbMode(), null,
                                    deployRequest.getUser(), deployRequest.getPassword());
        CreateInstancesResponse createInstanceResponse = instanceManager.createInstance(createInstanceRequest);
        destroyAwsContext();
        log.info("The public DNS of the instance is : " + createInstanceResponse.getAppInstancePublicDns());
        log.info("Completed deploy to region: " + deployRequest
                .getRegion() + " , env tag: " + deployRequest.getEnvTag());
        return new DeployResponse(createInstanceResponse.getAppInstancePublicDns(),
                createInstanceResponse.getDbInstancePrivateDns());
    }

    @Override
    public void undeploy(UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
        log.info("Started enterprise mode undeploy from region: " + undeployEnterpriseModeRequest
                .getRegion() + " , env tag: " + undeployEnterpriseModeRequest.getEnvTag());
        initAwsContext(undeployEnterpriseModeRequest.getRegion());
        enterpriseDeployManager.destroyEnvironment(undeployEnterpriseModeRequest);
        destroyAwsContext();
        log.info("Completed enterprise mode undeploy from region: " + undeployEnterpriseModeRequest
                .getRegion() + " , env tag: " + undeployEnterpriseModeRequest.getEnvTag());
    }

    @Override
    public void undeploy(UndeployRequest undeployRequest) {
        log.info("Started undeploy from region: " + undeployRequest
                .getRegion() + " , env tag: " + undeployRequest.getEnvTag());
        initAwsContext(undeployRequest.getRegion());
        DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest(undeployRequest.getEnvTag());
        instanceManager.destroyInstance(destroyInstanceRequest);
        DestroyNetworkRequest destroyNetworkRequest = new DestroyNetworkRequest(undeployRequest.getEnvTag());
        networkManager.destroyNetwork(destroyNetworkRequest);
        destroyAwsContext();
        log.info("Completed undeploy from region: " + undeployRequest
                .getRegion() + " , env tag: " + undeployRequest.getEnvTag());
    }

    public void uploadArtifact(String artifactName, String envTag){
        log.info("Started artifact upload for artifact: " + artifactName);
        log.debug("Override flag is: " + OVERRIDE_EXISTING_ARTIFACT);
        if(OVERRIDE_EXISTING_ARTIFACT) {
            String fullArtifactName = artifactName + ARTIFACT_EXTENSION;
            String bucketName = AwsEntitiesHelperUtil.getEntityName(envTag, BUCKET_ENTITY_TYPE).toLowerCase();
            AmazonS3 s3Client = AwsContext.getS3Client();
            if (!s3Client.doesBucketExist(bucketName)) {
                log.debug("Creating bucket: " + bucketName);
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName, AwsContext.getRegion().getName());
                s3Client.createBucket(createBucketRequest);
            } else {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
                listObjectsRequest.withBucketName(bucketName);
                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                boolean isArtifactExists = objectListing.getObjectSummaries()
                        .stream().filter(o -> o.getKey().equals(fullArtifactName)).findFirst().isPresent();
                if (isArtifactExists) {
                    log.debug("Deleting artifact: " + fullArtifactName + " from bucket: " + bucketName);
                    DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fullArtifactName);
                    s3Client.deleteObject(deleteObjectRequest);
                }
            }
            File file = new File(artifactsDir + "/" + fullArtifactName);
            s3Client.putObject(new PutObjectRequest(
                    bucketName, fullArtifactName, file));
            log.debug("Uploaded artifact: " + fullArtifactName + "to bucket: " + bucketName);
        }
        log.info("Finished artifact upload for artifact: " + artifactName);
    }

    private void initAwsContext(String region){
        Regions regions = Regions.fromName(region);
        AwsContext.init(regions, awsClientFactory.getEc2Client(regions),
                awsClientFactory.getS3Client(regions),
                awsClientFactory.getIAMClient(regions), awsClientFactory.getCloudFormationClient(regions));
    }

    private void destroyAwsContext(){
        AwsContext.destroy();
    }

}
