package org.teamspace.client.api;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.ApiException;
import org.teamspace.client.common.BaseTest;
import org.teamspace.client.common.domain.ApiResponseBody;
import org.teamspace.client.common.utils.ApiExceptionUtils;
import org.teamspace.client.model.Group;
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
public class GroupsTest extends BaseTest{


    @BeforeMethod
    public void setUp() throws ApiException{
        GroupApi groupApi = new GroupApi(getApiClient());
        groupApi.deleteAll();
    }

    @AfterMethod
    public void tearDown() throws ApiException {
        GroupApi groupApi = new GroupApi(getApiClient());
        groupApi.deleteAll();
    }
    
    @Test()
    public void testE2eScenario() throws ApiException{

        GroupApi groupApi = new GroupApi(getApiClient());

        //create 2 groups
        Group group1 = getGroup("group1");
        Group group2 = getGroup("group2");
        group1 = groupApi.create(group1);
        group2 = groupApi.create(group2);
        assertEquals(group1.getName(), "group1");
        assertEquals(group2.getName(), "group2");

        List<Group> groups = groupApi.findAll();
        assertEquals(groups.size(), 2);
        assertEquals(groups.stream().filter(g -> g.getName().equals("group2")).findAny().isPresent(), true);

        //update group
        group2.setName("group3");
        groupApi.update(group2.getId(), group2);
        group2 = groupApi.findOne(group2.getId());
        assertEquals(group2.getName(), "group3");

        //delete group
        groupApi.delete(group2.getId());
        groups = groupApi.findAll();
        assertEquals(groups.size(), 1);
        assertEquals(groups.stream().filter(g -> g.getName().equals("group1")).findAny().isPresent(), true);

        //delete all and return to original state
        groupApi.deleteAll();
        groups = groupApi.findAll();
        assertEquals(groups.size(), 0);
    }

    @Test()
    public void testCreateGroupNegativeScenario() throws ApiException, IOException {

        GroupApi groupApi = new GroupApi(getApiClient());

        //create group with the same name
        ApiResponseBody apiResponseBody = null;
        Group group1 = getGroup("group1");
        Group group2 = getGroup("group1");
        try {
            group1 = groupApi.create(group1);
            group2 = groupApi.create(group2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group with name: group1 already exists");

        //create group with null name
        apiResponseBody = null;
        try {
            group1 = getGroup(null);
            group1 = groupApi.create(group1);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group name can't be empty");

        //create group with empty name
        apiResponseBody = null;
        try {
            group1 = getGroup("  ");
            group1 = groupApi.create(group1);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group name can't be empty");

        //verify that only one group was created
        assertEquals(groupApi.findAll().size(), 1);
        assertTrue(groupApi.findAll().stream().anyMatch(g -> g.getName().equals("group1")));
    }

    @Test()
    public void testUpdateGroupNegativeScenario() throws ApiException, IOException {

        GroupApi groupApi = new GroupApi(getApiClient());

        //update 2 groups and then try update the second with the same name as first
        ApiResponseBody apiResponseBody = null;
        Group group1 = getGroup("group1");
        Group group2 = getGroup("group2");
        try {
            group1 = groupApi.create(group1);
            group2 = groupApi.create(group2);
            group2.setName("group1");
            group2 = groupApi.update(group2.getId(), group2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group with name: group1 already exists");

        //update group with null name
        apiResponseBody = null;
        try {
            group2.setName(null);
            group2 = groupApi.update(group2.getId(), group2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group name can't be empty");

        //update group with empty name
        apiResponseBody = null;
        try {
            group2.setName("   ");
            group2 = groupApi.update(group2.getId(), group2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "400");
        assertEquals(apiResponseBody.getMessage(), "Group name can't be empty");

        //update not existing group
        apiResponseBody = null;
        try {
            group2.setName("group7");
            group2.setId(7);
            group2 = groupApi.update(group2.getId(), group2);
        } catch (ApiException e){
            apiResponseBody = ApiExceptionUtils.fromJson(e.getResponseBody());
            log.error(e.getMessage(), e);
        }
        assertNotNull(apiResponseBody);
        assertEquals(apiResponseBody.getCode(), "404");
        assertEquals(apiResponseBody.getMessage(), "Group with ID " + 7 + " wasn't found");

        //verify that 2 groups with initial correct names remain
        assertEquals(groupApi.findAll().size(), 2);
        assertTrue(groupApi.findAll().stream().anyMatch(g -> g.getName().equals("group1")));
        assertTrue(groupApi.findAll().stream().anyMatch(g -> g.getName().equals("group2")));
    }


    private Group getGroup(String groupName){
        Group group = new Group();
        group.setName(groupName);
        return group;
    }


}
