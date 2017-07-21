package org.teamspace.network.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@AllArgsConstructor
public class CreateNetworkRequest {
    private String envTag;
    private String dbMode;
}
