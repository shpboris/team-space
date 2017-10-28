package org.teamspace.client.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.teamspace.client.ApiException;
import org.teamspace.client.api.users.UsersClient;
import org.teamspace.client.common.BaseTest;
import org.teamspace.client.common.config.ConfigurationManager;
import org.teamspace.client.config.CommonConfig;
import org.teamspace.client.model.User;
import org.testng.annotations.*;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.*;

/**
 * Created by shpilb on 07/04/2017.
 */
@Slf4j
public class UsersTest extends BaseTest{

    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    public static final String TEAM_SPACE_CLIENT_BASE_PACKAGE = "org.teamspace.client";
    public CommonConfig commonConfig = ConfigurationManager.getInstance().getConfiguration(CommonConfig.class);

    @BeforeMethod
    public void setUp() throws ApiException{
        UserApi userApi = new UserApi(getApiClient());
        userApi.deleteNonPrevilegedUsers();
    }

    @AfterMethod
    public void tearDown() {

    }
    
    @Test()
    public void testE2eScenario() throws ApiException{
        UserApi userApi = new UserApi(getApiClient());

        //create two users
        List<User> origUsers = userApi.findAll();
        User user1 = getUser("u1", "p1", "f1", "l1");
        User user2 = getUser("u2", "p2", "f2", "l2");
        user1 = userApi.create(user1);
        user2 = userApi.create(user2);
        List<User> currUsers = userApi.findAll();
        assertEquals(origUsers.size() + 2, currUsers.size());
        assertTrue(currUsers.stream().filter(u -> u.getUsername().equals("u1")).findAny().isPresent());

        //update user2
        user2.setFirstName("f1-new");
        userApi.update(user2.getId(), user2);
        User user2Updated = userApi.findOne(user2.getId());
        assertEquals(user2Updated.getFirstName(), "f1-new");

        //delete user1 and user2 and return to original state
        userApi.delete(user1.getId());
        userApi.delete(user2.getId());
        currUsers = userApi.findAll();
        assertEquals(origUsers.size(), currUsers.size());
        assertFalse(currUsers.stream().filter(u -> u.getUsername().equals("u1")).findAny().isPresent());
        assertFalse(currUsers.stream().filter(u -> u.getUsername().equals("u2")).findAny().isPresent());

    }

    @Test()
    public void testGetCurrentUser() throws ApiException{
        UserApi userApi = new UserApi(getApiClient());
        User currUser = null;
        currUser = userApi.getCurrentUser();
        log.info("current user " + currUser.getUsername());
        log.trace("current user trace" + currUser.getUsername());
        assertEquals(currUser.getUsername(), commonConfig.getUser());
    }

    @Test()
    public void testImportUsers() throws ApiException{
        UserApi userApi = new UserApi(getApiClient());

        //successful import
        int initialUsersCount = userApi.findAll().size();
        User user1 = getUser("u1", "p1", "f1", "l1");
        User user2 = getUser("u2", "p2", "f2", "l2");
        List<User> res = userApi.importUsers(Arrays.asList(user1, user2));
        User user1Imported = res.get(0);
        User user2Imported = res.get(1);
        assertEquals(res.size(), 2);
        assertEquals(user1Imported.getUsername(), "u1");
        int afterImportUsersCount = userApi.findAll().size();
        assertEquals(initialUsersCount + 2, afterImportUsersCount );

        //failure due to attempt to import existing user "user2"
        User user3 = getUser("u3", "p3", "f3", "l3");
        boolean exceptionThrown = false;
        try {
            userApi.importUsers(Arrays.asList(user3, user2));
        } catch (Exception e){
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
        int afterFailedImportUsersCount = userApi.findAll().size();
        assertEquals(afterImportUsersCount, afterFailedImportUsersCount);
    }

    @Test()
    public void testUsersClient() throws Exception{

        annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(TEAM_SPACE_CLIENT_BASE_PACKAGE);

        UsersClient usersClient = annotationConfigApplicationContext.getBean(UsersClient.class);
        User currentUser = usersClient.getCurrentUser();
        assertEquals(currentUser.getUsername(), commonConfig.getUser());

    }

    private User getUser(String username, String password, String firstName, String lastName){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(commonConfig.getUserRole());
        return user;
    }


}
