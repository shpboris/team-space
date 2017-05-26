package org.teamspace.instance.service;

import org.teamspace.instance.domain.DestroyInstanceRequest;

/**
 * Created by shpilb on 20/05/2017.
 */
public interface InstanceDestroyer {
    void destroyInstance(DestroyInstanceRequest destroyInstanceRequest);
}
