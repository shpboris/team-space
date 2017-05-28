package org.teamspace.aws.client.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
    private Map<Regions, AmazonIdentityManagement> regionsIamClientMap = new EnumMap<>(Regions.class);

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
}
