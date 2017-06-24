package org.teamspace.network.service.impl;

import com.amazonaws.services.ec2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.components.TagCreator;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.network.domain.DestroyNetworkRequest;
import org.teamspace.network.service.NetworkDestroyer;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
@Slf4j
public class NetworkDestroyerImpl implements NetworkDestroyer{

    @Autowired
    private TagCreator tagCreator;


    @Override
    public void destroyNetwork(DestroyNetworkRequest destroyNetworkRequest) {
        log.info("Started network deletion");
        String envTag = destroyNetworkRequest.getEnvTag();
        deleteRouteTables(envTag);
        deleteNatGateway(envTag);
        deleteGateway(envTag);
        deleteSubnets(envTag);
        deleteSecurityGroup(envTag);
        deleteVpc(envTag);
        log.info("Completed network deletion");
    }

    private void deleteVpc(String envTag){
        log.info("Deleting VPC ...");
        String vpcTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, VPC_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(vpcTagValue);
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(filter);
        DescribeVpcsResult describeVpcResult = AwsContext.getEc2Client().describeVpcs(describeVpcsRequest);
        describeVpcResult.getVpcs().stream().forEach(vpc -> {
            DeleteVpcRequest deleteVpcRequest = new DeleteVpcRequest();
            deleteVpcRequest.withVpcId(vpc.getVpcId());
            AwsContext.getEc2Client().deleteVpc(deleteVpcRequest);
            log.info("Deleted VPC: " + vpc.getVpcId());
        });

    }

    private void deleteSubnets(String envTag){
        log.info("Deleting subnets ...");
        String publicSubnetTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, PUBLIC_SUBNET_ENTITY_TYPE);
        String privateSubnetTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, PRIVATE_SUBNET_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(publicSubnetTagValue, privateSubnetTagValue);
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
        describeSubnetsRequest.withFilters(filter);
        boolean isSubnetsDeleted = false;
        int retriesNum = 0;
        while(!isSubnetsDeleted && retriesNum < MAX_RETRIES) {
            retriesNum++;
            try {
                Thread.sleep(WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for application connected state");
            }
            try {
                DescribeSubnetsResult describeSubnetResult = AwsContext.getEc2Client().describeSubnets(describeSubnetsRequest);
                describeSubnetResult.getSubnets().stream().forEach(subnet -> {
                    DeleteSubnetRequest deleteSubnetRequest = new DeleteSubnetRequest().withSubnetId(subnet.getSubnetId());
                    AwsContext.getEc2Client().deleteSubnet(deleteSubnetRequest);
                    log.info("Deleted subnet: " + subnet.getSubnetId());
                });
                isSubnetsDeleted = true;
            } catch (Exception e) {
                log.warn("Attempt #" + retriesNum + " to delete subnet failed");
            }
        }
    }

    private void deleteRouteTables(String envTag){
        log.info("Deleting route table ...");
        String publicRouteTableTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, PUBLIC_ROUTE_TABLE_ENTITY_TYPE);
        String privateRouteTableTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, PRIVATE_ROUTE_TABLE_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(publicRouteTableTagValue, privateRouteTableTagValue);
        DescribeRouteTablesRequest describeRouteTablesRequest = new DescribeRouteTablesRequest();
        describeRouteTablesRequest.withFilters(filter);
        DescribeRouteTablesResult describeRouteTablesResult = AwsContext.getEc2Client().describeRouteTables(describeRouteTablesRequest);
        describeRouteTablesResult.getRouteTables().stream().forEach(routeTable -> {
            routeTable.getAssociations().stream().forEach(routeTableAssociation -> {
                DisassociateRouteTableRequest disassociateRouteTableRequest = new DisassociateRouteTableRequest();
                disassociateRouteTableRequest.withAssociationId(routeTableAssociation.getRouteTableAssociationId());
                AwsContext.getEc2Client().disassociateRouteTable(disassociateRouteTableRequest);
                log.info("Deleted route table association for subnet: " + routeTableAssociation
                        .getSubnetId() + " and route table: " + routeTableAssociation.getRouteTableId());
            });
            DeleteRouteTableRequest deleteRouteTableRequest = new DeleteRouteTableRequest().withRouteTableId(routeTable.getRouteTableId());
            AwsContext.getEc2Client().deleteRouteTable(deleteRouteTableRequest);
            log.info("Deleted route table: " + routeTable.getRouteTableId());
        });

    }

    private void deleteGateway(String envTag) {
        log.info("Deleting gateway ...");
        String gatewayTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, GATEWAY_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(gatewayTagValue);
        DescribeInternetGatewaysRequest describeInternetGatewaysRequest = new DescribeInternetGatewaysRequest().withFilters(filter);
        DescribeInternetGatewaysResult internetGatewaysResult = AwsContext.getEc2Client().describeInternetGateways(describeInternetGatewaysRequest);

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
                                AwsContext.getEc2Client().detachInternetGateway(detachInternetGatewayRequest);

                            });
                            DeleteInternetGatewayRequest deleteInternetGatewayRequest = new DeleteInternetGatewayRequest();
                            deleteInternetGatewayRequest.withInternetGatewayId(internetGateway.getInternetGatewayId());
                            AwsContext.getEc2Client().deleteInternetGateway(deleteInternetGatewayRequest);
                            log.info("Deleted gateway: " + internetGateway.getInternetGatewayId());
                        }
                );
                isGatewayDeleted = true;
            }catch (Exception e){
                log.warn("Attempt #" + retriesNum + " to delete gateway failed");
            }

        }


    }

    private void deleteSecurityGroup(String envTag){
        log.info("Deleting security group ...");
        String securityGroupTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, SECURITY_GROUP_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(securityGroupTagValue);
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.withFilters(filter);
        DescribeSecurityGroupsResult securityGroupResult = AwsContext.getEc2Client().describeSecurityGroups(describeSecurityGroupsRequest);
        securityGroupResult.getSecurityGroups().stream().forEach(securityGroup -> {
            RevokeSecurityGroupIngressRequest revokeSecurityGroupIngressRequest = new RevokeSecurityGroupIngressRequest();
            revokeSecurityGroupIngressRequest.withGroupId(securityGroup.getGroupId());
            revokeSecurityGroupIngressRequest.withIpPermissions(securityGroup.getIpPermissions());
            AwsContext.getEc2Client().revokeSecurityGroupIngress(revokeSecurityGroupIngressRequest);

            DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
            deleteSecurityGroupRequest.withGroupId(securityGroup.getGroupId());
            AwsContext.getEc2Client().deleteSecurityGroup(deleteSecurityGroupRequest);
            log.info("Deleted security group: " + securityGroup.getGroupId());
        });
    }

    private void deleteNatGateway(String envTag){
        log.info("Deleting NAT gateway ...");
        String vpcTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, VPC_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(vpcTagValue);
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(filter);
        DescribeVpcsResult describeVpcResult = AwsContext.getEc2Client().describeVpcs(describeVpcsRequest);
        String vpcId = describeVpcResult.getVpcs().get(0).getVpcId();

        DescribeNatGatewaysRequest describeNatGatewaysRequest = new DescribeNatGatewaysRequest();
        DescribeNatGatewaysResult natGatewaysResult = AwsContext.getEc2Client().describeNatGateways(describeNatGatewaysRequest);
        natGatewaysResult.getNatGateways().stream().forEach(natGateway -> {
            if (natGateway.getVpcId().equals(vpcId) && !natGateway.getState().equals("deleted")) {
                DeleteNatGatewayRequest deleteNatGatewayRequest = new DeleteNatGatewayRequest();
                deleteNatGatewayRequest.withNatGatewayId(natGateway.getNatGatewayId());
                AwsContext.getEc2Client().deleteNatGateway(deleteNatGatewayRequest);
                log.info("Deleted NAT gateway: " + natGateway.getNatGatewayId());
                natGateway.getNatGatewayAddresses().stream().forEach(natGatewayAddress -> {
                    log.info("Releasing elastic IP ...");
                    boolean isAddressReleased = false;
                    int retriesNum = 0;
                    while(!isAddressReleased && retriesNum < MAX_RETRIES) {
                        retriesNum++;
                        try {
                            Thread.sleep(WAIT_TIME_MILLISEC);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Unable to wait for address release");
                        }
                        try {
                            AwsContext.getEc2Client().releaseAddress(new ReleaseAddressRequest().withAllocationId(natGatewayAddress.getAllocationId()));
                            log.info("Released allocation: " + natGatewayAddress.getAllocationId());
                            isAddressReleased = true;
                        } catch (Exception e) {
                            log.warn("Attempt #" + retriesNum + " to release address failed");
                        }
                    }
                });
            }
        });
    }


}
