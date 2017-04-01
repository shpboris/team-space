package org.teamspace.auth.domain;

import lombok.Data;

/**
 * Created by shpilb on 10/01/2017.
 */
@Data
public class TokenResponseData {

    private String accessToken;

    private String tokenType;
}
