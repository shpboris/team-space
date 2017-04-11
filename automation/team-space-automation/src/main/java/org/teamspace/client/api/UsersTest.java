package org.teamspace.client.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.users.UsersClient;
import org.teamspace.client.common.BaseTest;
import org.teamspace.client.common.Constants;
import org.teamspace.client.model.User;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by shpilb on 07/04/2017.
 */
@Slf4j
public class UsersTest extends BaseTest{

    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    public static final String TEAM_SPACE_CLIENT_BASE_PACKAGE = "org.teamspace.client";

    @Test()
    public void testGetCurrentUser() throws ApiException{
        UserApi userApi = new UserApi(getApiClient());
        User currUser = null;
        currUser = userApi.getCurrentUser();
        log.info("current user " + currUser.getUsername());
        log.trace("current user trace" + currUser.getUsername());
        assertEquals(currUser.getUsername(), Constants.USER);
    }

    @Test()
    public void testUsersClient() throws Exception{

        annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(TEAM_SPACE_CLIENT_BASE_PACKAGE);

        UsersClient usersClient = annotationConfigApplicationContext.getBean(UsersClient.class);
        User currentUser = usersClient.getCurrentUser();
        assertEquals(currentUser.getUsername(), Constants.USER);

    }


}
