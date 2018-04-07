package org.teamspace.aws.client.context;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;

/**
 * Created by shpilb on 27/05/2017.
 */
public class AwsContext {
    private static final ThreadLocal<Regions> region = new ThreadLocal<Regions>();
    private static final ThreadLocal<AmazonEC2> ec2Client = new ThreadLocal<AmazonEC2>();
    private static final ThreadLocal<AmazonS3> s3Client = new ThreadLocal<AmazonS3>();
    private static final ThreadLocal<AmazonIdentityManagement> iamClient = new ThreadLocal<AmazonIdentityManagement>();
    private static final ThreadLocal<AmazonCloudFormation> cloudFormationClient = new ThreadLocal<AmazonCloudFormation>();
    private static final ThreadLocal<AmazonSQS> sqsClient = new ThreadLocal<AmazonSQS>();

    public static void init(Regions regions, AmazonEC2 amazonEC2,
                            AmazonS3 amazonS3,
                            AmazonIdentityManagement amazonIdentityManagement,
                            AmazonCloudFormation amazonCloudFormation, AmazonSQS amazonSQS){
        region.set(regions);
        ec2Client.set(amazonEC2);
        s3Client.set(amazonS3);
        iamClient.set(amazonIdentityManagement);
        cloudFormationClient.set(amazonCloudFormation);
        sqsClient.set(amazonSQS);
    }

    public static void destroy(){
        region.remove();
        ec2Client.remove();
        s3Client.remove();
        iamClient.remove();
        cloudFormationClient.remove();
        sqsClient.remove();
    }

    public static AmazonEC2 getEc2Client(){
        return ec2Client.get();
    }

    public static AmazonS3 getS3Client(){
        return s3Client.get();
    }

    public static AmazonIdentityManagement getIamClient(){
        return iamClient.get();
    }

    public static AmazonCloudFormation getCloudFormationClient(){
        return cloudFormationClient.get();
    }

    public static AmazonSQS getSqsClient(){
        return sqsClient.get();
    }

    public static Regions getRegion(){
        return region.get();
    }
}
