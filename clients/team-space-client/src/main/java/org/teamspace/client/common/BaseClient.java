package org.teamspace.client.common;

import org.teamspace.client.ApiClient;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.AuthenticationApi;
import org.teamspace.client.utils.AuthHelperUtil;

import java.util.Map;

/**
 * Created by shpilb on 08/04/2017.
 */
public class BaseClient {

    private ApiClient apiClient;
    private static final String USER = "user1";
    private static final String PASS = "pass1";
    private static final String HOST = "localhost";
    private static final String GRANT_TYPE = "password";

    public BaseClient(){
        this.apiClient = new ApiClient();
        apiClient.setBasePath(AuthHelperUtil.getHostBasePath(HOST));
    }

    public ApiClient getApiClient(){
        return apiClient;

    }

    public void acquireToken() throws ApiException {
        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        Map<String, Object> respMap = authenticationApi.postForToken(GRANT_TYPE, USER, PASS);
        String token = (String) respMap.get("access_token");
        apiClient.setAccessToken(token);
    }
}
