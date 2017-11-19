package org.teamspace.cloud_formation.service;

import com.amazonaws.services.cloudformation.model.*;
import org.teamspace.aws.client.context.AwsContext;

import java.util.List;

public interface StackManagerService {
    CreateStackResult createStack(String stackName, String templateClassPathLocation, List<Parameter> parameters);
    Stack waitForStackCreation(String stackName, int maxRetriesCount);
    String getStackOutput(Stack stack, String outputKey);
    String getStackOutput(String stackName, String outputKey);
    public void deleteStack(String stackName);
    public Stack waitForStackDeletion(String stackName, int maxRetriesCount);
}
