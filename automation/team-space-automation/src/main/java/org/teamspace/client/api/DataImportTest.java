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

    public static final Long IMPORT_JOB_TIME_TO_COMPLETE = 3000l;
    public static final Long IMPORT_JOB_TIME_TO_COMPLETE_STEP = 2000l;

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


    @Test()
    public void testImportWithControlledProgress() throws Exception{

        //run import flow
        DataImportApi dataImportApi = new DataImportApi(getApiClient());
        DataImportRequest dataImportRequest = new DataImportRequest();
        dataImportRequest.setShouldReportCompletion(true);
        dataImportRequest.setShouldPerformCleanup(true);
        dataImportRequest.setForceControlledProgress(true);
        DataImportResult dataImportResult = dataImportApi.create(dataImportRequest);

        //test users. groups, membership reader steps started in parallel
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        DataImportResult returnedImportResult = dataImportApi.findAll()
                .stream()
                .filter(importRes -> importRes.getJobExecutionId().equals(dataImportResult.getJobExecutionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find relevant import result"));

        returnedImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals("usersDataReaderStep"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("usersDataReaderStep wasn't started"));

        returnedImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals("groupsDataReaderStep"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("groupsDataReaderStep wasn't started"));

        returnedImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals("membershipsDataReaderStep"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("membershipsDataReaderStep wasn't started"));

        //allow progress and test that data writer step started
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = dataImportApi.findAll()
                .stream()
                .filter(importRes -> importRes.getJobExecutionId().equals(dataImportResult.getJobExecutionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find relevant import result"));

        returnedImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals("dataWriterStep"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("dataWriterStep wasn't started"));

        //allow progress and test that report completion step started
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = dataImportApi.findAll()
                .stream()
                .filter(importRes -> importRes.getJobExecutionId().equals(dataImportResult.getJobExecutionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find relevant import result"));

        returnedImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals("reportCompletionStep"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("reportCompletionStep wasn't started"));

        //allow progress and verify job completion
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = dataImportApi.findAll()
                .stream()
                .filter(importRes -> importRes.getJobExecutionId().equals(dataImportResult.getJobExecutionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find relevant import result"));

        assertEquals(returnedImportResult.getRunningSteps().size(), 0);
        assertEquals(returnedImportResult.getStatus(), "COMPLETED");
    }
}
