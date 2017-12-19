package org.teamspace.deploy_common.domain;

import lombok.Data;

@Data
public class UndeployEnterpriseModeRequest extends UndeployRequest {
    private boolean singleStackDeployment = true;
}
