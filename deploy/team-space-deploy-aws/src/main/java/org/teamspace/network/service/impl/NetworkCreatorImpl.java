package org.teamspace.network.service.impl;

import com.amazonaws.services.ec2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.components.TagCreator;
import org.teamspace.network.domain.CreateNetworkRequest;
import org.teamspace.network.domain.CreateNetworkResponse;
import org.teamspace.network.service.NetworkCreator;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Slf4j
@Component
public class NetworkCreatorImpl implements NetworkCreator{

    @Autowired
    private TagCreator tagCreator;


    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkRequest createNetworkRequest) {
        log.info("Started network creation");
        String envTag = createNetworkRequest.getEnvTag();
        Vpc vpc = createVpc("10.0.0.0/16", envTag);
        Subnet publicSubnet = createSubnet(vpc, "10.0.0.0/24", envTag);
        Subnet privateSubnet = createSubnet(vpc, "10.0.1.0/24", envTag);

        InternetGateway internetGateway = createInternetGateway(envTag);
        attachGatewayToVpc(vpc, internetGateway);
        RouteTable routeTable = createRouteTable(vpc, envTag);
        createRoute(internetGateway, routeTable);
        associateRouteTableWithSubnet(routeTable, publicSubnet);
        mapPublicIpOnLaunch(publicSubnet);
        String securityGroupId = createSecurityGroup("HTTP-SSH-SG", vpc, envTag);
        authorizeSecurityGroupIngress(securityGroupId, "tcp", SSH_PORT, "0.0.0.0/0");
        authorizeSecurityGroupIngress(securityGroupId, "tcp", HTTP_PORT, "0.0.0.0/0");
        CreateNetworkResponse createNetworkResponse =
                new CreateNetworkResponse(publicSubnet.getSubnetId(), privateSubnet.getSubnetId(), securityGroupId);
        log.info("Completed network creation");
        return createNetworkResponse;
    }

    private Vpc createVpc(String cidrBlock, String envTag){
        log.info("Creating VPC ...");
        CreateVpcRequest createVpcRequest = new CreateVpcRequest(cidrBlock);
        CreateVpcResult res = AwsContext.getEc2Client().createVpc(createVpcRequest);
        Vpc vpc = res.getVpc();
        tagCreator.createTag(vpc.getVpcId(), VPC_ENTITY_TYPE, envTag);
        enableDnsHostnames(vpc);
        log.info("Created VPC: " + vpc.getVpcId());
        return vpc;
    }

    private Subnet createSubnet(Vpc vpc, String cidrBlock, String envTag){
        log.info("Creating subnet ...");
        CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest(vpc.getVpcId(), cidrBlock);
        CreateSubnetResult createSubnetResult = AwsContext.getEc2Client().createSubnet(createSubnetRequest);
        Subnet subnet = createSubnetResult.getSubnet();
        tagCreator.createTag(subnet.getSubnetId(), SUBNET_ENTITY_TYPE, envTag);
        log.info("Created subnet: " + subnet.getSubnetId() + " in VPC: " + vpc.getVpcId());
        return subnet;
    }

    private InternetGateway createInternetGateway(String envTag){
        log.info("Creating internet gateway ...");
        CreateInternetGatewayRequest createInternetGatewayRequest = new CreateInternetGatewayRequest();
        CreateInternetGatewayResult createInternetGatewayResult = AwsContext.getEc2Client().createInternetGateway();
        InternetGateway internetGateway = createInternetGatewayResult.getInternetGateway();
        tagCreator.createTag(internetGateway.getInternetGatewayId(), GATEWAY_ENTITY_TYPE, envTag);
        log.info("Created internet gateway: " + internetGateway.getInternetGatewayId());
        return internetGateway;
    }


    private void attachGatewayToVpc(Vpc vpc, InternetGateway internetGateway){
        log.info("Attaching internet gateway ...");
        AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest();
        attachInternetGatewayRequest.withVpcId(vpc.getVpcId()).withInternetGatewayId(internetGateway.getInternetGatewayId());
        AttachInternetGatewayResult attachInternetGatewayResult = AwsContext.getEc2Client()
                .attachInternetGateway(attachInternetGatewayRequest);
        log.info("Attached gateway: " + internetGateway.getInternetGatewayId() + " to VPC: " + vpc.getVpcId());
    }

    private RouteTable createRouteTable(Vpc vpc, String envTag){
        log.info("Creating route table ...");
        CreateRouteTableRequest createRouteTableRequest = new CreateRouteTableRequest();
        createRouteTableRequest.withVpcId(vpc.getVpcId());
        CreateRouteTableResult createRouteTableResult = AwsContext.getEc2Client().createRouteTable(createRouteTableRequest);
        RouteTable routeTable = createRouteTableResult.getRouteTable();
        tagCreator.createTag(routeTable.getRouteTableId(), ROUTE_TABLE_ENTITY_TYPE, envTag);
        log.info("Created route table: " + routeTable.getRouteTableId());
        return routeTable;
    }

    private void createRoute(InternetGateway internetGateway, RouteTable routeTable){
        log.info("Creating route ...");
        CreateRouteRequest createRouteRequest = new CreateRouteRequest();
        createRouteRequest.withGatewayId(internetGateway.getInternetGatewayId())
                .withRouteTableId(routeTable.getRouteTableId())
                .withDestinationCidrBlock("0.0.0.0/0");
        CreateRouteResult createRouteResult = AwsContext.getEc2Client().createRoute(createRouteRequest);
        log.info("Created route");
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet){
        log.info("Associating route table with subnet ...");
        AssociateRouteTableRequest associateRouteTableRequest = new AssociateRouteTableRequest();
        associateRouteTableRequest.withRouteTableId(routeTable.getRouteTableId())
                .withSubnetId(subnet.getSubnetId());
        AssociateRouteTableResult associateRouteTableResult = new AssociateRouteTableResult();
        associateRouteTableResult = AwsContext.getEc2Client().associateRouteTable(associateRouteTableRequest);
        log.info("Associated route table: " + routeTable.getRouteTableId() + " with subnet " + subnet.getSubnetId());
    }

    private String createSecurityGroup(String groupName, Vpc vpc, String envTag){
        log.info("Creating security group ...");
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
        createSecurityGroupRequest.withVpcId(vpc.getVpcId())
                .withGroupName(groupName).withDescription(groupName + " Description");
        CreateSecurityGroupResult createSecurityGroupResult = AwsContext.getEc2Client()
                .createSecurityGroup(createSecurityGroupRequest);
        String securityGroupId = createSecurityGroupResult.getGroupId();
        tagCreator.createTag(securityGroupId, SECURITY_GROUP_ENTITY_TYPE, envTag);
        log.info("Created security group: " + securityGroupId);
        return securityGroupId;
    }

    private void authorizeSecurityGroupIngress(String groupId, String protocol, Integer port, String cidr){
        log.info("Creating ingress security authorization ...");
        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
        authorizeSecurityGroupIngressRequest.withGroupId(groupId)
                .withIpProtocol(protocol).withFromPort(port).withToPort(port).withCidrIp(cidr);
        AwsContext.getEc2Client().authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
        log.info("Created ingress security authorization");
    }

    private void mapPublicIpOnLaunch(Subnet subnet){
        log.info("Mapping public IPs on launch ...");
        ModifySubnetAttributeRequest modifySubnetAttributeRequest = new ModifySubnetAttributeRequest();
        modifySubnetAttributeRequest.withSubnetId(subnet.getSubnetId());
        modifySubnetAttributeRequest.setMapPublicIpOnLaunch(true);
        AwsContext.getEc2Client().modifySubnetAttribute(modifySubnetAttributeRequest);
        log.info("Mapped public IPs on launch for subnet: " + subnet.getSubnetId());
    }

    private void enableDnsHostnames(Vpc vpc){
        ModifyVpcAttributeRequest modifyVpcAttributeRequest = new ModifyVpcAttributeRequest()
                .withVpcId(vpc.getVpcId())
                .withEnableDnsHostnames(true);

        AwsContext.getEc2Client().modifyVpcAttribute(modifyVpcAttributeRequest);

    }

}