package org.teamspace.deploy.domain;

import lombok.Data;

@Data
public class UndeployEnterpriseModeRequest extends UndeployRequest {
    private boolean waitForCompletion = true;
}
