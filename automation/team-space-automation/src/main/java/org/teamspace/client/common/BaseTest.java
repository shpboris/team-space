package org.teamspace.client.common;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.ApiClient;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.AuthenticationApi;
import org.teamspace.client.common.config.ConfigurationManager;
import org.teamspace.client.config.CommonConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by shpilb on 11/04/2017.
 */
@Slf4j
public class BaseTest {

    public ApiClient getApiClient(){
        CommonConfig commonConfig = ConfigurationManager.getInstance().getConfiguration(CommonConfig.class);
        ApiClient apiClient = new ApiClient();
        apiClient.setVerifyingSsl(false);
        apiClient.setBasePath(commonConfig.getBasePath());
        apiClient.setConnectTimeout(commonConfig.getTimeout() * 1000);
        //prevents read timeout failures - especially relevant for debugging
        apiClient.getHttpClient().setReadTimeout(commonConfig.getTimeout(), TimeUnit.SECONDS);

        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        try {
            Map<String, Object> respMap = authenticationApi
                    .postForToken(commonConfig.getGrantType(), commonConfig.getUser(), commonConfig.getPassword());
            String token = (String) respMap.get("access_token");
            apiClient.setAccessToken(token);
        } catch (ApiException e) {
            log.error("Failed to create ApiClient", e);
        }
        return apiClient;
    }
}
