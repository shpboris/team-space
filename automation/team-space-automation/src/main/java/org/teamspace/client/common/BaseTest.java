package org.teamspace.client.common;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.ApiClient;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.AuthenticationApi;

import java.util.Map;

import static org.teamspace.client.common.Constants.TIMEOUT_SEC;

/**
 * Created by shpilb on 11/04/2017.
 */
@Slf4j
public class BaseTest {

    public ApiClient getApiClient(){
        ApiClient apiClient = new ApiClient();
        apiClient.setVerifyingSsl(false);
        apiClient.setBasePath(Constants.BASE_PATH);
        apiClient.setConnectTimeout(TIMEOUT_SEC * 1000);

        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        try {
            Map<String, Object> respMap = authenticationApi.postForToken(Constants.GRANT_TYPE, Constants.USER, Constants.PASSWORD);
            String token = (String) respMap.get("access_token");
            apiClient.setAccessToken(token);
        } catch (ApiException e) {
            log.error("Failed to create ApiClient", e);
        }
        return apiClient;
    }
}
