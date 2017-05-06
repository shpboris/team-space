package org.teamspace.aws.client.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.AwsClientFactory;

import javax.annotation.PostConstruct;

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

    @PostConstruct
    private void init(){
        awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    @Override
    public AmazonEC2 getEc2Client() {
        AmazonEC2 ec2Client;
        if(awsCredentials != null) {
            ec2Client = AmazonEC2ClientBuilder.standard()
                    .withRegion(Regions.DEFAULT_REGION)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
        } else{
            throw new RuntimeException("Unable to obtain EC2 client");
        }
        return ec2Client;
    }

    @Override
    public AmazonS3 getS3Client() {
        AmazonS3 amazonS3Client;
        if(awsCredentials != null) {
            amazonS3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.DEFAULT_REGION)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
        } else{
            throw new RuntimeException("Unable to obtain S3 client");
        }
        return amazonS3Client;
    }
}
