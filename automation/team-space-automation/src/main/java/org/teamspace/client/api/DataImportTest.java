package org.teamspace.client.api;

import org.teamspace.client.common.BaseTest;
import org.teamspace.client.model.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by shpilb on 06/10/2017.
 */
public class DataImportTest extends BaseTest {

    public static final Long IMPORT_JOB_TIME_TO_COMPLETE = 2000l;

    @Test()
    public void testSimpleImport() throws Exception{

        //run import flow
        DataImportApi dataImportApi = new DataImportApi(getApiClient());
        DataImportRequest dataImportRequest = new DataImportRequest();
        dataImportRequest.setShouldReportCompletion(true);
        dataImportRequest.setShouldPerformCleanup(true);
        dataImportApi.create(dataImportRequest);

        //wait until WF is done
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE);

        //test users
        UserApi userApi = new UserApi(getApiClient());
        List<User> users = userApi.findAll();
        assertEquals(users.size(),5);
        assertEquals(users.get(1).getFirstName(), "user1FirstName");

        //test groups
        GroupApi groupApi = new GroupApi(getApiClient());
        List<Group> groups = groupApi.findAll();
        assertEquals(groups.size(),3);
        assertEquals(groups.get(2).getName(),"group3Name");

        //test membership
        MembershipApi membershipApi = new MembershipApi(getApiClient());
        List<Membership> memberships = membershipApi.findAll();
        assertEquals(memberships.size(),3);
        Membership membership = memberships.get(2);
        assertEquals(membership.getId().intValue(), 3);

        assertEquals(membership.getGroup().getId().intValue(), 2);
        assertEquals(membership.getGroup().getName(), "group2Name");

        assertEquals(membership.getUser().getId().intValue(), 4);
        assertEquals(membership.getUser().getFirstName(), "user3FirstName");
    }
}
