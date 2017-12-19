package org.teamspace.deploy_common.domain;

import lombok.Data;

/**
 * Created by shpilb on 06/05/2017.
 */
@Data
public class DeployRequest {
    private String cloudType;
    private String region;
    private String envTag;
    private String artifactName;
    private String user;
    private String password;
    protected String dbMode;
}
