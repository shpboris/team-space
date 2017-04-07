import org.teamspace.client.ApiClient;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.AuthenticationApi;
import org.teamspace.client.api.UserApi;
import org.teamspace.client.model.User;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by shpilb on 07/04/2017.
 */
public class UsersTest {
    @Test()
    public void testUsers(){
        ApiClient apiClient = new ApiClient();
        apiClient.setVerifyingSsl(false);
        apiClient.setBasePath("https://localhost:443/rest");

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
        User currUser = null;
        try {
            currUser = userApi.getCurrentUser();
            System.out.println("User ----  " + currUser.getUsername());
        } catch (ApiException e) {
            e.printStackTrace();
        }
        assertEquals("user1", currUser.getUsername());
    }
}
