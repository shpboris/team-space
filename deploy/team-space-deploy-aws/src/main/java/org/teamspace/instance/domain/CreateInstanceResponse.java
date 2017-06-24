package org.teamspace.instance.domain;

import lombok.*;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstanceResponse {
    private String appInstancePublicDns;
    private String dbInstancePrivateDns;
}
