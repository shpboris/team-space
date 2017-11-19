package org.teamspace.cloud_formation.service.impl;

import com.amazonaws.services.cloudformation.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.aws.client.context.AwsContext;
import org.teamspace.cloud_formation.service.StackManagerService;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.teamspace.commons.constants.DeploymentConstants.*;

@Service
@Slf4j
public class StackManagerServiceImpl implements StackManagerService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public CreateStackResult createStack(String stackName,
                                         String templateClassPathLocation, List<Parameter> parameters) {
        log.info("Started creation of stack {}", stackName);
        CreateStackResult createStackResult = null;
        try {
            CreateStackRequest createStackRequest = new CreateStackRequest().withStackName(stackName);
            createStackRequest.setParameters(parameters);
            //required when creating IAM instance profile
            createStackRequest.setCapabilities(Collections.singletonList("CAPABILITY_NAMED_IAM"));
            Resource resource = resourceLoader.getResource(templateClassPathLocation);
            InputStream inputStream = resource.getInputStream();
            String cloudFormationTemplate = IOUtils.toString(inputStream, "UTF-8");
            createStackRequest.setTemplateBody(cloudFormationTemplate);
            createStackResult = AwsContext.getCloudFormationClient().createStack(createStackRequest);
            log.info("Completed creation of stack {}", stackName);
        } catch (Exception e){
            throw new RuntimeException("Unable to create stack", e);
        }
        return createStackResult;
    }

    @Override
    public Stack waitForStackCreation(String stackName, int maxRetriesCount){
        log.info("Started waiting for stack creation completion of stack {}", stackName);
        int retriesCount = 0;
        boolean isStackCreationCompleted = false;
        Stack stack = null;
        try {
            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
            while (!isStackCreationCompleted && retriesCount < maxRetriesCount) {
                retriesCount++;
                Thread.sleep(CF_STACK_CREATION_WAIT_TIME_MILLISEC);
                stack = AwsContext.getCloudFormationClient().
                        describeStacks(describeStacksRequest).getStacks().get(0);
                if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                        stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                        stack.getStackStatus().equals(StackStatus.ROLLBACK_COMPLETE.toString()) ||
                        stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString())) {
                    isStackCreationCompleted = true;
                }
                log.debug("Waiting for stack creation: attempt #{}, stack status is {}", retriesCount, stack.getStackStatus());
            }
            log.info("Finished waiting for stack creation completion of stack {}, stack status is {}", stackName, stack.getStackStatus());
        } catch (Exception e){
            throw new RuntimeException("Unable to wait for stack creation", e);
        }
        if (!stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString())) {
            log.error("Stack creation failed with stack status {} and reason {}",
                    stack.getStackStatus(), stack.getStackName());
            throw new RuntimeException("Stack creation failed");
        }
        return stack;
    }

    public String getStackOutput(Stack stack, String outputKey){
        return stack.getOutputs().stream()
                .filter(output -> output.getOutputKey().equals(outputKey)).findFirst().get().getOutputValue();
    }

    public String getStackOutput(String stackName, String outputKey){
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
        DescribeStacksResult describeStacksResult = AwsContext.getCloudFormationClient().describeStacks(describeStacksRequest);
        List<Output> stackOutputs = describeStacksResult.getStacks().get(0).getOutputs();
        return stackOutputs.stream()
                .filter(output -> output.getOutputKey().equals(outputKey)).findFirst().get().getOutputValue();
    }

    public void deleteStack(String stackName){
        log.info("Started deletion of stack {}", stackName);
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.withStackName(stackName);
        AwsContext.getCloudFormationClient().deleteStack(deleteStackRequest);
        log.info("Completed deletion of stack {}", stackName);
    }

    public Stack waitForStackDeletion(String stackName, int maxRetriesCount) {
        log.info("Started waiting for stack deletion completion of stack {}", stackName);
        List<Stack> stacks = null;
        Stack stack = null;
        String stackStatus = null;
        int retriesCount = 0;
        boolean isStackDeletionCompleted = false;
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        describeStacksRequest.withStackName(stackName);

        try {
            while (!isStackDeletionCompleted && retriesCount < maxRetriesCount) {
                retriesCount++;
                Thread.sleep(CF_STACK_CREATION_WAIT_TIME_MILLISEC);
                try {
                    stacks = AwsContext.getCloudFormationClient().
                            describeStacks(describeStacksRequest).getStacks();
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    stack = null;
                    stackStatus = StackStatus.DELETE_COMPLETE.toString();
                    break;
                }
                stack = stacks.get(0);
                stackStatus = stack.getStackStatus();
                if (stackStatus.equals(StackStatus.DELETE_FAILED.toString())
                        || stackStatus.equals(StackStatus.DELETE_COMPLETE.toString())) {
                    isStackDeletionCompleted = true;
                }
                log.debug("Waiting for stack deletion: attempt #{}, stack status is {}", retriesCount, stackStatus);
            }
        } catch (Exception e){
            throw new RuntimeException("Unable to wait for stack deletion completion", e);
        }
        log.info("Finished waiting for stack deletion completion of stack {}, stack status is {}", stackName, stackStatus);
        return stack;
    }
}
