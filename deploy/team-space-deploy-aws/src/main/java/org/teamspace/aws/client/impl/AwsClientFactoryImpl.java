package org.teamspace.aws.client.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.AwsClientFactory;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by shpilb on 06/05/2017.
 */
@Component
public class AwsClientFactoryImpl implements AwsClientFactory {

    @Value("${accessKey}")
    private String accessKey;

    @Value("${secretKey}")
    private String secretKey;

    private BasicAWSCredentials awsCredentials;

    private Map<Regions, AmazonEC2> regionsToEc2ClientMap = new EnumMap<>(Regions.class);
    private Map<Regions, AmazonS3> regionsToS3ClientMap = new EnumMap<>(Regions.class);
    private Map<Regions, AmazonCloudFormation> regionsToCloudFormationClientMap = new EnumMap<>(Regions.class);
    private Map<Regions, AmazonIdentityManagement> regionsIamClientMap = new EnumMap<>(Regions.class);
    private Map<Regions, AmazonSQS> regionsSqsClientMap = new EnumMap<>(Regions.class);

    @PostConstruct
    private void init(){
        awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    @Override
    public synchronized AmazonEC2 getEc2Client(Regions region) {
        AmazonEC2 ec2Client;
        if(regionsToEc2ClientMap.get(region) != null){
            ec2Client = regionsToEc2ClientMap.get(region);
        }else {
            if (awsCredentials != null) {
                ec2Client = AmazonEC2ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .build();
                regionsToEc2ClientMap.put(region, ec2Client);
            } else {
                throw new RuntimeException("Unable to obtain EC2 client");
            }
        }
        return ec2Client;
    }

    @Override
    public synchronized AmazonS3 getS3Client(Regions region) {
        AmazonS3 amazonS3Client;
        if(regionsToS3ClientMap.get(region) != null){
            amazonS3Client = regionsToS3ClientMap.get(region);
        }else {
            if (awsCredentials != null) {
                amazonS3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .build();
                regionsToS3ClientMap.put(region, amazonS3Client);
            } else {
                throw new RuntimeException("Unable to obtain S3 client");
            }
        }
        return amazonS3Client;
    }

    @Override
    public synchronized AmazonCloudFormation getCloudFormationClient(Regions region) {
        AmazonCloudFormation cloudFormationClient;
        if(regionsToCloudFormationClientMap.get(region) != null){
            cloudFormationClient = regionsToCloudFormationClientMap.get(region);
        } else {
            if (awsCredentials != null) {
                cloudFormationClient = AmazonCloudFormationClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .withClientConfiguration(new ClientConfiguration().withConnectionTimeout(30000))
                        .build();
                regionsToCloudFormationClientMap.put(region, cloudFormationClient);
            } else {
                throw new RuntimeException("Unable to obtain Cloud Formation client");
            }
        }
        return cloudFormationClient;
    }

    @Override
    public synchronized AmazonIdentityManagement getIAMClient(Regions region) {
        AmazonIdentityManagement amazonIdentityManagementClient;
        if(regionsIamClientMap.get(region) != null){
            amazonIdentityManagementClient = regionsIamClientMap.get(region);
        }else {
            if (awsCredentials != null) {
                amazonIdentityManagementClient = AmazonIdentityManagementClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .build();
                regionsIamClientMap.put(region, amazonIdentityManagementClient);
            } else {
                throw new RuntimeException("Unable to obtain S3 client");
            }
        }
        return amazonIdentityManagementClient;
    }

    @Override
    public AmazonSQS getSqsClient(Regions region) {
        AmazonSQS amazonSqsClient;
        if(regionsSqsClientMap.get(region) != null){
            amazonSqsClient = regionsSqsClientMap.get(region);
        } else {
            if (awsCredentials != null) {
                amazonSqsClient = AmazonSQSClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .build();
                regionsSqsClientMap.put(region, amazonSqsClient);
            } else {
                throw new RuntimeException("Unable to obtain SQS client");
            }
        }
        return amazonSqsClient;
    }
}
