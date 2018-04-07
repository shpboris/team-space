package org.teamspace.instance.service.impl;

import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.sqs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.deploy_common.domain.AddInstanceEnterpriseModeRequest;
import org.teamspace.deploy_common.domain.RemoveInstanceEnterpriseModeRequest;
import org.teamspace.instance.domain.*;
import org.teamspace.instance.service.*;

import java.util.List;

import static org.teamspace.commons.constants.DeploymentConstants.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
@Slf4j
public class InstanceManagerImpl implements InstanceManager {

    @Autowired
    private InstanceCreator instanceCreator;

    @Autowired
    private InstanceDestroyer instanceDestroyer;

    @Override
    public CreateInstancesResponse createInstance(CreateInstancesRequest createInstanceRequest) {
        CreateInstancesResponse createInstanceResponse = instanceCreator.createInstances(createInstanceRequest);
        return createInstanceResponse;
    }

    @Override
    public void destroyInstance(DestroyInstanceRequest destroyInstanceRequest) {
        instanceDestroyer.destroyInstance(destroyInstanceRequest);
    }

    @Override
    public void addInstance(AddInstanceEnterpriseModeRequest addInstanceEnterpriseModeRequest) {
        int initialInstancesCount = getRunningInstancesCount(addInstanceEnterpriseModeRequest.getEnvTag());
        String queueUrl = sendMessage("CreateInstanceRequestsQueue", "addInstanceRequestMsg");
        int retiresCount = 0;
        boolean isInstanceCreated = false;
        while (!isInstanceCreated && retiresCount < AUTO_SCALE_MAX_RETRIES){
            retiresCount++;
            try {
                Thread.sleep(AUTO_SCALE_WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                String msg = "Failed to wait for instance addition";
                log.error(msg);
                throw new RuntimeException(msg);
            }
            int updatedInstancesCount = getRunningInstancesCount(addInstanceEnterpriseModeRequest.getEnvTag());
            if(initialInstancesCount + 1 == updatedInstancesCount){
                processMessage(queueUrl);
                isInstanceCreated = true;
            }
        }
        if (!isInstanceCreated) {
            throw new RuntimeException("Failed adding instance");
        }
    }

    @Override
    public void removeInstance(RemoveInstanceEnterpriseModeRequest removeInstanceEnterpriseModeRequest) {
        int initialInstancesCount = getRunningInstancesCount(removeInstanceEnterpriseModeRequest.getEnvTag());
        String queueUrl = sendMessage("RemoveInstanceRequestsQueue", "removeInstanceRequestMsg");
        int retiresCount = 0;
        boolean isInstanceRemoved = false;
        while (!isInstanceRemoved && retiresCount < AUTO_SCALE_MAX_RETRIES){
            retiresCount++;
            try {
                Thread.sleep(AUTO_SCALE_WAIT_TIME_MILLISEC);
            } catch (InterruptedException e) {
                String msg = "Failed to wait for instance removal";
                log.error(msg);
                throw new RuntimeException(msg);
            }
            int updatedInstancesCount = getRunningInstancesCount(removeInstanceEnterpriseModeRequest.getEnvTag());
            if(initialInstancesCount - 1 == updatedInstancesCount){
                processMessage(queueUrl);
                isInstanceRemoved = true;
            }
        }
        if (!isInstanceRemoved) {
            throw new RuntimeException("Failed removing instance");
        }
    }

    @Override
    public int getRunningInstancesCount(String envTag){
        String appInstanceTagValue = AwsEntitiesHelperUtil
                .getEntityName(envTag, APP_INSTANCE_ENTITY_TYPE);
        Filter filter = new Filter().withName("tag:" + TAG_NAME).withValues(appInstanceTagValue, appInstanceTagValue);
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withFilters(filter);
        DescribeInstancesResult describeInstancesResult = AwsContext.getEc2Client().describeInstances(describeInstancesRequest);
        int instancesCount = 0;
        for (Reservation reservation :
                describeInstancesResult.getReservations()) {
            instancesCount += reservation.getInstances().stream()
                    .filter(instance -> instance.getState().getName().equals(INSTANCE_STATE_RUNNING)).count();
        }
        return instancesCount;
    }

    private String sendMessage(String queueName, String msgBody){
        GetQueueUrlResult getQueueUrlResult = AwsContext.getSqsClient().getQueueUrl(queueName);
        String queueUrl = getQueueUrlResult.getQueueUrl();
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withQueueUrl(queueUrl);
        sendMessageRequest.withMessageBody(msgBody);
        AwsContext.getSqsClient().sendMessage(sendMessageRequest);
        return queueUrl;
    }

    private void processMessage(String queueUrl){
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        receiveMessageRequest.withMaxNumberOfMessages(1);
        List<Message> messages = AwsContext.getSqsClient().receiveMessage(receiveMessageRequest).getMessages();
        if(!CollectionUtils.isEmpty(messages)) {
            Message message = messages.get(0);
            log.info("Received message is: {}, deleting it from queue", message.getBody());
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
            AwsContext.getSqsClient().deleteMessage(deleteMessageRequest);
        }
    }

}
