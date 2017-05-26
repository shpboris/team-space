package org.teamspace.network.service.impl;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.AwsClientFactory;
import org.teamspace.commons.components.TagCreator;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.network.domain.DestroyNetworkRequest;
import org.teamspace.network.service.NetworkDestroyer;

import javax.annotation.PostConstruct;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
@Slf4j
public class NetworkDestroyerImpl implements NetworkDestroyer{

    @Autowired
    private AwsClientFactory awsClientFactory;

    @Autowired
    private TagCreator tagCreator;

    private AmazonEC2 ec2Client;

    private AmazonS3 s3Client;

    private AmazonIdentityManagement iamClient;


    @PostConstruct
    private void initClients(){
        ec2Client = awsClientFactory.getEc2Client();
        s3Client = awsClientFactory.getS3Client();
        iamClient = awsClientFactory.getIAMClient();
    }

    @Override
    public void destroyNetwork(DestroyNetworkRequest destroyNetworkRequest) {
        String envTag = destroyNetworkRequest.getEnvTag();
        deleteRouteTable(envTag);
        deleteGateway(envTag);
        deleteSubnets(envTag);
        deleteSecurityGroup(envTag);
        deleteVpc(envTag);
    }

    private void deleteVpc(String envTag){
        String vpcTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, VPC_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(vpcTagValue);
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(filter);
        DescribeVpcsResult describeVpcResult = ec2Client.describeVpcs(describeVpcsRequest);
        describeVpcResult.getVpcs().stream().forEach(vpc -> {
            DeleteVpcRequest deleteVpcRequest = new DeleteVpcRequest();
            deleteVpcRequest.withVpcId(vpc.getVpcId());
            ec2Client.deleteVpc(deleteVpcRequest);
        });

    }

    private void deleteSubnets(String envTag){
        String subnetTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, SUBNET_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(subnetTagValue);
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
        describeSubnetsRequest.withFilters(filter);
        DescribeSubnetsResult describeSubnetResult = ec2Client.describeSubnets(describeSubnetsRequest);
        describeSubnetResult.getSubnets().stream().forEach(subnet -> {
            DeleteSubnetRequest deleteSubnetRequest = new DeleteSubnetRequest().withSubnetId(subnet.getSubnetId());
            ec2Client.deleteSubnet(deleteSubnetRequest);
        });
    }

    private void deleteRouteTable(String envTag){
        String routeTableTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, ROUTE_TABLE_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(routeTableTagValue);
        DescribeRouteTablesRequest describeRouteTablesRequest = new DescribeRouteTablesRequest();
        describeRouteTablesRequest.withFilters(filter);
        DescribeRouteTablesResult describeRouteTablesResult = ec2Client.describeRouteTables(describeRouteTablesRequest);
        describeRouteTablesResult.getRouteTables().stream().forEach(routeTable -> {
            routeTable.getAssociations().stream().forEach(routeTableAssociation -> {
                DisassociateRouteTableRequest disassociateRouteTableRequest = new DisassociateRouteTableRequest();
                disassociateRouteTableRequest.withAssociationId(routeTableAssociation.getRouteTableAssociationId());
                ec2Client.disassociateRouteTable(disassociateRouteTableRequest);
            });
            DeleteRouteTableRequest deleteRouteTableRequest = new DeleteRouteTableRequest().withRouteTableId(routeTable.getRouteTableId());
            ec2Client.deleteRouteTable(deleteRouteTableRequest);
        });

    }

    private void deleteGateway(String envTag) {
        String gatewayTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, GATEWAY_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(gatewayTagValue);
        DescribeInternetGatewaysRequest describeInternetGatewaysRequest = new DescribeInternetGatewaysRequest().withFilters(filter);
        DescribeInternetGatewaysResult internetGatewaysResult = ec2Client.describeInternetGateways(describeInternetGatewaysRequest);

        boolean isGatewayDeleted = false;
        int retriesNum = 0;
        while(!isGatewayDeleted && retriesNum < MAX_RETRIES) {
            retriesNum++;
            try {
                Thread.sleep(WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for application connected state");
            }
            try {
                internetGatewaysResult.getInternetGateways().stream().forEach(internetGateway -> {
                            internetGateway.getAttachments().stream().forEach(internetGatewayAttachment -> {
                                DetachInternetGatewayRequest detachInternetGatewayRequest = new DetachInternetGatewayRequest();
                                detachInternetGatewayRequest.withVpcId(internetGatewayAttachment.getVpcId())
                                        .withInternetGatewayId(internetGateway.getInternetGatewayId());
                                ec2Client.detachInternetGateway(detachInternetGatewayRequest);

                            });
                            DeleteInternetGatewayRequest deleteInternetGatewayRequest = new DeleteInternetGatewayRequest();
                            deleteInternetGatewayRequest.withInternetGatewayId(internetGateway.getInternetGatewayId());
                            ec2Client.deleteInternetGateway(deleteInternetGatewayRequest);
                        }
                );
                isGatewayDeleted = true;
            }catch (Exception e){
                log.warn("Attempt #" + retriesNum + " to delete gateway failed");
            }

        }


    }

    private void deleteSecurityGroup(String envTag){
        String securityGroupTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, SECURITY_GROUP_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(securityGroupTagValue);
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.withFilters(filter);
        DescribeSecurityGroupsResult securityGroupResult = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest);
        securityGroupResult.getSecurityGroups().stream().forEach(securityGroup -> {
            RevokeSecurityGroupIngressRequest revokeSecurityGroupIngressRequest = new RevokeSecurityGroupIngressRequest();
            revokeSecurityGroupIngressRequest.withGroupId(securityGroup.getGroupId());
            revokeSecurityGroupIngressRequest.withIpPermissions(securityGroup.getIpPermissions());
            ec2Client.revokeSecurityGroupIngress(revokeSecurityGroupIngressRequest);

            DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
            deleteSecurityGroupRequest.withGroupId(securityGroup.getGroupId());
            ec2Client.deleteSecurityGroup(deleteSecurityGroupRequest);
        });
    }


}
