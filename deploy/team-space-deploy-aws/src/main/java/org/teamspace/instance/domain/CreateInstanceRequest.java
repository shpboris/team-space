package org.teamspace.instance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@AllArgsConstructor
public class CreateInstanceRequest {
    private String envTag;
    private String subnetId;
    private String securityGroupId;
    private String artifactName;
}
