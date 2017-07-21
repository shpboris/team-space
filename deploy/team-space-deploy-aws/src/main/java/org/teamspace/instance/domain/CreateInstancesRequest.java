package org.teamspace.instance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@AllArgsConstructor
public class CreateInstancesRequest {
    private String envTag;
    private String publicSubnetId;
    private String privateSubnetIdFirstAz;
    private String privateSubnetIdSecondAz;
    private String securityGroupId;
    private String artifactName;
    private String dbMode;
    private String dbInstancePrivateDns;
    private String user;
    private String password;
}
