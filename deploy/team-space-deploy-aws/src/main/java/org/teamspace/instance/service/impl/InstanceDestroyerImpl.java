package org.teamspace.instance.service.impl;

import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.identitymanagement.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.instance.domain.DestroyInstanceRequest;
import org.teamspace.instance.service.InstanceDestroyer;

import java.util.Optional;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
@Slf4j
public class InstanceDestroyerImpl implements InstanceDestroyer {

    @Override
    public void destroyInstance(DestroyInstanceRequest destroyInstanceRequest) {
        String envTag = destroyInstanceRequest.getEnvTag();
        deleteKeyPair(envTag);
        deleteInstanceProfile(envTag);
        deleteInstance(envTag);
    }

    private void deleteKeyPair(String envTag){
        String keyPairName = AwsEntitiesHelperUtil.
                getEntityName(envTag, KEY_PAIR_ENTITY_TYPE);

        DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest();
        deleteKeyPairRequest.withKeyName(keyPairName);
        AwsContext.getEc2Client().deleteKeyPair(deleteKeyPairRequest);
    }

    private void deleteInstanceProfile(String envTag){
        String profileName = AwsEntitiesHelperUtil
                .getEntityName(envTag, PROFILE_AND_ROLE_ENTITY_TYPE);
        try {
            RemoveRoleFromInstanceProfileRequest removeRoleFromInstanceProfileRequest = new RemoveRoleFromInstanceProfileRequest();
            removeRoleFromInstanceProfileRequest.withInstanceProfileName(profileName).withRoleName(profileName);
            AwsContext.getIamClient().removeRoleFromInstanceProfile(removeRoleFromInstanceProfileRequest);

            DeleteInstanceProfileRequest deleteInstanceProfileRequest = new DeleteInstanceProfileRequest();
            deleteInstanceProfileRequest.withInstanceProfileName(profileName);
            AwsContext.getIamClient().deleteInstanceProfile(deleteInstanceProfileRequest);

            ListAttachedRolePoliciesRequest listAttachedRolePoliciesRequest = new ListAttachedRolePoliciesRequest();
            listAttachedRolePoliciesRequest.withRoleName(profileName);
            ListAttachedRolePoliciesResult listAttachedRolePoliciesResult = AwsContext.getIamClient().listAttachedRolePolicies(listAttachedRolePoliciesRequest);
            Optional<AttachedPolicy> policy = listAttachedRolePoliciesResult.getAttachedPolicies()
                    .stream().filter(p -> profileName.equals(p.getPolicyName())).findFirst();

            DetachRolePolicyRequest detachRolePolicyRequest = new DetachRolePolicyRequest();
            detachRolePolicyRequest.withRoleName(profileName).withPolicyArn(policy.get().getPolicyArn());
            AwsContext.getIamClient().detachRolePolicy(detachRolePolicyRequest);

            DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest();
            deletePolicyRequest.withPolicyArn(policy.get().getPolicyArn());
            AwsContext.getIamClient().deletePolicy(deletePolicyRequest);

            DeleteRoleRequest deleteRoleRequest = new DeleteRoleRequest();
            deleteRoleRequest.withRoleName(profileName);
            AwsContext.getIamClient().deleteRole(deleteRoleRequest);
        }catch (Exception e){
            log.warn("Couldn't delete instance profile", e);
        }
    }

    private void deleteInstance(String envTag){
        String instanceTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, INSTANCE_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(instanceTagValue);
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withFilters(filter);
        DescribeInstancesResult describeInstancesResult = AwsContext.getEc2Client().describeInstances(describeInstancesRequest);
        describeInstancesResult.getReservations().stream().forEach(reservation -> {
            reservation.getInstances().stream().forEach(instance -> {
               if(!instance.getState().equals(INSTANCE_STATE_TERMINATED)){
                   TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
                   terminateInstancesRequest.withInstanceIds(instance.getInstanceId());
                   AwsContext.getEc2Client().terminateInstances(terminateInstancesRequest);
               }
            });
        });
    }
}
