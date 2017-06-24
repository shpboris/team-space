package org.teamspace.instance.service;

import org.teamspace.instance.domain.CreateInstancesRequest;
import org.teamspace.instance.domain.CreateInstancesResponse;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface InstanceCreator {
    CreateInstancesResponse createInstances(CreateInstancesRequest createInstanceRequest);
}
