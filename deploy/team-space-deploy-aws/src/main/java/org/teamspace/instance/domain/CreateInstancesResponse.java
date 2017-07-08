package org.teamspace.instance.domain;

import lombok.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstancesResponse {
    private String appInstancePublicDns;
    private String dbInstancePrivateDns;
    private String dbUrl;
}
