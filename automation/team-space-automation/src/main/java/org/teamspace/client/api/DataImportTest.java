package org.teamspace.client.api;

import org.teamspace.client.common.BaseTest;
import org.teamspace.client.model.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

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

        //wait for job completion
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE);

        //verify data
        verifyDataAfterImport();
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

        //test users, groups and membership reader steps started in parallel
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        DataImportResult returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "usersDataReaderStep");
        verifyStepRunning(returnedImportResult, "groupsDataReaderStep");
        verifyStepRunning(returnedImportResult, "membershipsDataReaderStep");

        //allow progress and test that data writer step started
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "dataWriterStep");

        //allow progress and test that report completion step started
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "reportCompletionStep");

        //allow progress and verify job completion
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        assertEquals(returnedImportResult.getRunningSteps().size(), 0);
        assertEquals(returnedImportResult.getStatus(), "COMPLETED");

        verifyDataAfterImport();
    }

    @Test()
    public void testImportStopAndRestartWithControlledProgress() throws Exception{
        //run import flow
        DataImportApi dataImportApi = new DataImportApi(getApiClient());
        DataImportRequest dataImportRequest = new DataImportRequest();
        dataImportRequest.setShouldReportCompletion(true);
        dataImportRequest.setShouldPerformCleanup(true);
        dataImportRequest.setForceControlledProgress(true);
        DataImportResult dataImportResult = dataImportApi.create(dataImportRequest);

        //test users, groups and membership reader steps started in parallel
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        DataImportResult returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "usersDataReaderStep");
        verifyStepRunning(returnedImportResult, "groupsDataReaderStep");
        verifyStepRunning(returnedImportResult, "membershipsDataReaderStep");

        //allow progress and test that data writer step started
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "dataWriterStep");

        //stop the flow and verify that job execution status changed to STOPPING, data writer step is still running
        dataImportApi.stop(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        verifyStepRunning(returnedImportResult, "dataWriterStep");
        assertEquals(returnedImportResult.getStatus(), "STOPPING");

        //allow progress to finish data writer step and test that job execution status changed to STOPPED
        dataImportApi.allowProgress(returnedImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        returnedImportResult = getJobExecution(dataImportApi, dataImportResult.getJobExecutionId());
        assertEquals(returnedImportResult.getStatus(), "STOPPED");

        //restart the flow and verify that new execution started and data writer step is executed again
        DataImportResult newImportResult = dataImportApi.restart(returnedImportResult.getJobExecutionId());
        assertNotSame(returnedImportResult.getJobExecutionId(), newImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        newImportResult = getJobExecution(dataImportApi, newImportResult.getJobExecutionId());
        verifyStepRunning(newImportResult, "dataWriterStep");
        assertTrue(newImportResult.getStatus().equals("STARTING") || newImportResult.getStatus().equals("STARTED"));

        //allow progress and test that report completion step started
        dataImportApi.allowProgress(newImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        newImportResult = getJobExecution(dataImportApi, newImportResult.getJobExecutionId());
        verifyStepRunning(newImportResult, "reportCompletionStep");

        //allow progress and verify job completion
        dataImportApi.allowProgress(newImportResult.getJobExecutionId());
        Thread.sleep(IMPORT_JOB_TIME_TO_COMPLETE_STEP);
        newImportResult = getJobExecution(dataImportApi, newImportResult.getJobExecutionId());
        assertEquals(newImportResult.getRunningSteps().size(), 0);
        assertEquals(newImportResult.getStatus(), "COMPLETED");

        verifyDataAfterImport();
    }

    private void verifyDataAfterImport() throws Exception{
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

    private DataImportResult getJobExecution(DataImportApi dataImportApi, Long executionId) throws Exception{
        DataImportResult importResult = dataImportApi.findAll()
                .stream()
                .filter(importRes -> importRes.getJobExecutionId().equals(executionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find relevant import result by execution id"));
        return importResult;
    }

    private void verifyStepRunning(DataImportResult dataImportResult, String stepName){
        dataImportResult.getRunningSteps()
                .stream()
                .filter(step -> step.equals(stepName))
                .findAny()
                .orElseThrow(() -> new RuntimeException(stepName + " wasn't started"));
    }
}
