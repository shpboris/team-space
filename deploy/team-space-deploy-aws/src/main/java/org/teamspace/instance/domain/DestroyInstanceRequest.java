package org.teamspace.instance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 20/05/2017.
 */
@Data
@AllArgsConstructor
public class DestroyInstanceRequest {
    private String envTag;
}
