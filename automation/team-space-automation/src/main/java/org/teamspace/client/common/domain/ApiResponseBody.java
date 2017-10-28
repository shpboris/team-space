package org.teamspace.client.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shpilb on 07/04/2017.
 */
@AllArgsConstructor
@Data
public class ApiResponseBody {
    private String code;
    private String message;
    private String status;
}
