package org.teamspace.deploy.service.impl;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teamspace.aws.client.AwsClientFactory;
import org.teamspace.deploy.domain.DeployRequest;
import org.teamspace.deploy.domain.DeployResponse;
import org.teamspace.deploy.service.DeployService;

/**
 * Created by shpilb on 06/05/2017.
 */
@Service
public class DeployServiceImpl implements DeployService{

    @Autowired
    private AwsClientFactory awsClientFactory;


    @Override
    public DeployResponse deploy(DeployRequest deployRequest) {
        AmazonEC2 ec2Client = awsClientFactory.getEc2Client();
        DescribeInstancesResult instances = ec2Client.describeInstances();
        String dns = instances.getReservations().get(1).getInstances().get(0).getPublicDnsName();
        return new DeployResponse(dns);
    }
}
