import org.teamspace.client.ApiClient;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.AuthenticationApi;
import org.teamspace.client.api.UserApi;
import org.teamspace.client.model.User;

import java.util.Map;

/**
 * Created by shpilb on 18/02/2017.
 */
public class UsersClient {
    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient();
        //apiClient.setVerifyingSsl(false);
        apiClient.setBasePath("http://localhost:8888/rest");

        //apiClient.setSslCaCert()

        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        try {
            Map<String, Object> respMap = authenticationApi.postForToken("password", "user1", "pass1");
            String token = (String) respMap.get("access_token");
            apiClient.setAccessToken(token);
        } catch (ApiException e) {
            e.printStackTrace();
        }


        UserApi userApi = new UserApi(apiClient);
        try {
            User currUser = userApi.getCurrentUser();
            System.out.println("User ----  " + currUser.getUsername());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
