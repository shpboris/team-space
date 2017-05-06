package org.teamspace.aws.client;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface AwsClientFactory {
    AmazonEC2 getEc2Client();
    AmazonS3 getS3Client();
}
