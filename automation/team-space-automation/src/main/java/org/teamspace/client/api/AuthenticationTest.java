package org.teamspace.client.api;

import org.teamspace.client.auth.OAuth;
import org.teamspace.client.common.BaseTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by shpilb on 11/04/2017.
 */
public class AuthenticationTest extends BaseTest{
    @Test()
    public void testAuthentication(){
        assertNotNull(getToken());
    }

    private String getToken(){
        OAuth auth = (OAuth)getApiClient().getAuthentication("team_space_auth");
        return auth.getAccessToken();
    }
}
