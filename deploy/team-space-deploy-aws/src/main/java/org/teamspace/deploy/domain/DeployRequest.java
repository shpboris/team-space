package org.teamspace.deploy.domain;

import lombok.Data;

/**
 * Created by shpilb on 06/05/2017.
 */
@Data
public class DeployRequest {
    private String region;
    private String envTag;
    private String artifactName;
}
