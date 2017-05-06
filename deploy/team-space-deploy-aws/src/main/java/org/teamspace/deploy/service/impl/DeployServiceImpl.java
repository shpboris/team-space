package org.teamspace.deploy.service.impl;

import org.springframework.stereotype.Service;
import org.teamspace.deploy.service.DeployService;

/**
 * Created by shpilb on 06/05/2017.
 */
@Service
public class DeployServiceImpl implements DeployService{
    @Override
    public String deploy(String artifactLocation) {
        return "aws-dns";
    }
}
