package org.teamspace.deploy.service;

import org.teamspace.deploy.domain.DeployRequest;
import org.teamspace.deploy.domain.DeployResponse;

/**
 * Created by shpilb on 06/05/2017.
 */
public interface DeployService {
    public DeployResponse deploy(DeployRequest deployRequest);
}
