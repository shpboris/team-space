package org.teamspace.deploy_common.domain;

import lombok.Data;

/**
 * Created by shpilb on 06/05/2017.
 */
@Data
public class InstancesDetailsEnterpriseModeRequest {
    private String cloudType;
    private String region;
    private String envTag;
}
