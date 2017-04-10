package org.teamspace.client.common;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.user}")
    private String user;

    @Value("${server.password}")
    private String password;

    @Value("${server.host}")
    private String host;

    @Value("${server.grantType}")
    private String grantType;


    public BaseClient(){
        this.apiClient = new ApiClient();
    }

    public ApiClient getApiClient(){
        return apiClient;

    }

    public void acquireToken() throws ApiException {
        apiClient.setBasePath(AuthHelperUtil.getHostBasePath(host));
        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        Map<String, Object> respMap = authenticationApi.postForToken(grantType, user, password);
        String token = (String) respMap.get("access_token");
        apiClient.setAccessToken(token);
    }
}
