package org.teamspace.client.api;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.ApiException;
import org.teamspace.client.common.BaseTest;
import org.teamspace.client.model.Group;
import org.testng.annotations.*;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

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


    private Group getGroup(String groupName){
        Group group = new Group();
        group.setName(groupName);
        return group;
    }


}
