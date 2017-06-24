package org.teamspace.instance.service;

import org.teamspace.instance.domain.CreateInstanceRequest;
import org.teamspace.instance.domain.CreateInstanceResponse;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface InstanceCreator {
    CreateInstanceResponse createInstances(CreateInstanceRequest createInstanceRequest);
}
