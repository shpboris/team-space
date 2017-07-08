package org.teamspace.deploy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 06/05/2017.
 */
@Data
@AllArgsConstructor
public class DeployResponse {
    private String appInstancePublicDns;
    private String dbInstancePrivateDns;
}
