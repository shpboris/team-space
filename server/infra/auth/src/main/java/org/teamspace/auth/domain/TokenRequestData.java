package org.teamspace.auth.domain;

import lombok.Data;

/**
 * Created by shpilb on 10/01/2017.
 */
@Data
public class TokenRequestData {
    private String grantType;

    private String username;

    private String password;

    private String clientId;
}
