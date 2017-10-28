package org.teamspace.client.api;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.ApiException;
import org.teamspace.client.common.BaseTest;
import org.teamspace.client.common.config.ConfigurationManager;
import org.teamspace.client.common.domain.ApiResponseBody;
import org.teamspace.client.common.utils.ApiExceptionUtils;
import org.teamspace.client.config.CommonConfig;
import org.teamspace.client.model.*;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by shpilb on 07/04/2017.
 */
@Slf4j
public class MembershipsTest extends BaseTest{

    public CommonConfig commonConfig = ConfigurationManager.getInstance().getConfiguration(CommonConfig.class);

    @BeforeMethod
    public void setUp() throws ApiException{
        MembershipApi membershipApi = new MembershipApi(getApiClient());
        membershipApi.deleteAll();

        GroupApi groupApi = new GroupApi(getApiClient());
        groupApi.deleteAll();

        UserApi userApi = new UserApi(getApiClient());
        userApi.deleteNonPrevilegedUsers();
    }

    @AfterMethod
    public void tearDown() throws ApiException {
        MembershipApi membershipApi = new MembershipApi(getApiClient());
        membershipApi.deleteAll();

        GroupApi groupApi = new GroupApi(getApiClient());
        groupApi.deleteAll();

        UserApi userApi = new UserApi(getApiClient());
        userApi.deleteNonPrevilegedUsers();
    }
    
    @Test()
    public void testE2eScenario() throws ApiException{

        UserApi userApi = new UserApi(getApiClient());
        GroupApi groupApi = new GroupApi(getApiClient());
        MembershipApi membershipApi = new MembershipApi(getApiClient());

        //create 3 users
        User user1 = getUser("u1", "p1", "f1", "l1");
        User user2 = getUser("u2", "p2", "f2", "l2");
        User user3 = getUser("u3", "p3", "f3", "l3");
        user1 = userApi.create(user1);
        user2 = userApi.create(user2);
        user3 = userApi.create(user3);
        assertEquals(user1.getUsername(), "u1");
        assertEquals(user2.getUsername(), "u2");
        assertEquals(user3.getUsername(), "u3");

        //create 2 groups
        Group group1 = getGroup("g1");
        Group group2 = getGroup("g2");
        group1 = groupApi.create(group1);
        group2 = groupApi.create(group2);
        assertEquals(group1.getName(), "g1");
        assertEquals(group2.getName(), "g2");

        //match users 1,2 to group 1 and user 3 to group 2
        Membership membership1 = getMembership(user1, group1);
        Membership membership2 = getMembership(user2, group1);
        Membership membership3 = getMembership(user3, group2);
        membership1 = membershipApi.create(membership1);
        membership2 = membershipApi.create(membership2);
        membership3 = membershipApi.create(membership3);

        //verify created memberships
        List<Membership> membershipList = membershipApi.findAll();
        assertEquals(membershipList.size(), 3);
        boolean res = membershipList.stream().anyMatch(membership -> {
            return membership.getUser().getUsername().equals("u1")&&
                    membership.getGroup().getName().equals("g1");
        });
        assertTrue(res);

        res = membershipList.stream().anyMatch(membership -> {
            return membership.getUser().getUsername().equals("u2")&&
                    membership.getGroup().getName().equals("g1");
        });
        assertTrue(res);

        res = membershipList.stream().anyMatch(membership -> {
            return membership.getUser().getUsername().equals("u3")&&
                    membership.getGroup().getName().equals("g2");
        });
        assertTrue(res);


        //delete group 1 and verify its memberships were
        //deleted - i.e it is not connected to users 1 and 2
        //using findAllRaw API to make sure that memberships are REALLY removed from DB
        //and not just absent in result because of join
        groupApi.delete(group1.getId());
        membershipList = membershipApi.findAllRaw();
        assertEquals(membershipList.size(), 1);

        res = false;
        for(Membership currMembership : membershipList){
            if(currMembership.getUser().getId().equals(membership3.getUser().getId())
                    && currMembership.getGroup().getId().equals(membership3.getGroup().getId())){
                res = true;
            }
        }
        assertTrue(res);

        //delete user3 and verify its membership was deleted
        //i.e it is not connected to group 2 anymore
        userApi.delete(user3.getId());
        membershipList = membershipApi.findAllRaw();
        assertEquals(membershipList.size(), 0);

        //verify that finally remaining users are only 1 and 2 and remaining group is 2
        List<Group> groups = groupApi.findAll();
        assertEquals(groups.size(), 1);
        assertEquals(groups.stream().filter(g -> g.getName().equals("g2")).findAny().isPresent(), true);

        List<User> users = userApi.findAll();
        assertEquals(users.size(), 3);
        assertTrue(users.stream().filter(u -> u.getUsername().equals("u1")).findAny().isPresent());
        assertTrue(users.stream().filter(u -> u.getUsername().equals("u2")).findAny().isPresent());

        //match user 1 to group2
        membership1 = getMembership(user1, group2);
        membership1 = membershipApi.create(membership1);
        assertEquals(membership1.getUser().getUsername(), "u1");
        assertEquals(membership1.getGroup().getName(), "g2");
        membershipList = membershipApi.findAll();
        assertEquals(membershipList.size(), 1);

        //now update membership by matching user 2 to group2
        membershipApi.delete(membership1.getId());
        membership1 = getMembership(user2, group2);
        membership1 = membershipApi.create(membership1);
        assertEquals(membership1.getUser().getUsername(), "u2");
        assertEquals(membership1.getGroup().getName(), "g2");
        membershipList = membershipApi.findAll();
        assertEquals(membershipList.size(), 1);

        //delete all users and verify that memberships are deleted
        userApi.deleteNonPrevilegedUsers();
        membershipList = membershipApi.findAllRaw();
        assertEquals(membershipList.size(), 0);
    }

    @Test()
    public void testCreateMembershipNegativeScenario() throws ApiException, IOException {
        UserApi userApi = new UserApi(getApiClient());
        GroupApi groupApi = new GroupApi(getApiClient());
        MembershipApi membershipApi = new MembershipApi(getApiClient());

        //create 3 users
        User user1 = getUser("u1", "p1", "f1", "l1");
        user1 = userApi.create(user1);
        assertEquals(user1.getUsername(), "u1");

        //create 2 groups
        Group group1 = getGroup("g1");
        group1 = groupApi.create(group1);
        assertEquals(group1.getName(), "g1");

        //match users 1 to group 1 twice
        ApiResponseBody apiResponseBody = null;
        Membership membership1 = getMembership(user1, group1);
        Membership membership2 = getMembership(user1, group1);

        try {
            membership1 = membershipApi.create(membership1);
            membership2 = membershipApi.create(membership2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        String errMsg = String.format("Membership for user: %s and group: %s already exists", user1.getId(), group1.getId());
        assertEquals(apiResponseBody.getMessage(), errMsg);


        //test not existing user id
        apiResponseBody = null;
        //this user is not created yet and has no id - hence not valid
        User notValidUser = getUser("u1", "p1", "f1", "l1");
        Membership failedMembership = getMembership(notValidUser, group1);
        try {
            failedMembership = membershipApi.create(failedMembership);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Membership should have not empty references to group and user");


        //test group equals to null
        apiResponseBody = null;
        failedMembership = getMembership(user1, null);
        try {
            failedMembership = membershipApi.create(failedMembership);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Membership should have not empty references to group and user");


        //test user and group having not existing id's
        apiResponseBody = null;
        notValidUser = getUser("u1", "p1", "f1", "l1");
        notValidUser.setId(7);
        Group notValidGroup = getGroup("g1");
        notValidGroup.setId(8);
        failedMembership = getMembership(notValidUser, notValidGroup);
        try {
            failedMembership = membershipApi.create(failedMembership);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Membership should point to valid existing user and group");

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

    private Group getGroup(String groupName){
        Group group = new Group();
        group.setName(groupName);
        return group;
    }

    private Membership getMembership(User user, Group group){
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setGroup(group);
        return membership;
    }




}
