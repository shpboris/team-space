package org.teamspace.network.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@Wither
@AllArgsConstructor
public class CreateNetworkResponse {
    private String publicSubnetId;
    private String privateSubnetId;
    private String securityGroupId;
}
