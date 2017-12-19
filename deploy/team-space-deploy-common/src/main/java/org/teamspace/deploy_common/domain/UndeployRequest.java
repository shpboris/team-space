package org.teamspace.deploy_common.domain;

import lombok.Data;

/**
 * Created by shpilb on 26/05/2017.
 */
@Data
public class UndeployRequest {
    private String region;
    private String envTag;
}
