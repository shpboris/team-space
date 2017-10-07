package org.teamspace.data_import.resources;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.domain.DataImportResult;
import org.teamspace.data_import.job_config.parameters_registry.JobParametersRegistry;
import org.teamspace.data_import.job_config.progress_manager.JobProgressManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.teamspace.data_import.constants.DataImportConstants.CUSTOM_PARAMETERS_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.DATA_IMPORT_JOB_NAME;
import static org.teamspace.data_import.constants.DataImportConstants.MAX_JOB_INSTANCES_COUNT;

/**
 * Created by shpilb on 21/01/2017.
 */
@Api(value = "Data Import"/*,
        authorizations = {@Authorization("team_space_auth")}*/)
@Path("/data-import")
@Produces(MediaType.APPLICATION_JSON)
//@PermitAll
@Component
@Slf4j
public class DataImportResource {

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobParametersRegistry jobParametersRegistry;

    @Autowired
    private JobProgressManager jobProgressManager;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "initiate data import", response = DataImportResult.class)
    public Response create(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest dataImportRequest) {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        DataImportResult dataImportResult = new DataImportResult();
        try {
            String jobParameterValue = getJobParameterValue();
            updateJobParameterRegistry(jobParameterValue, dataImportRequest);
            Long executionId = jobOperator.start(DATA_IMPORT_JOB_NAME, getJobParametersStr(jobParameterValue));
            JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
            dataImportResult.setJobInstanceId(jobExecution.getJobInstance().getId());
            dataImportResult.setJobExecutionId(jobExecution.getId());
            dataImportResult.setStatus(BatchStatus.STARTING.toString());
        } catch (Exception e) {
            log.error("Failed to import data", e);
        }
        
        return Response.status(Response.Status.CREATED).entity(dataImportResult).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get data import operations", response = DataImportResult.class, responseContainer = "List")
    public Response findAll() {
        List<DataImportResult> dataImportResults = new ArrayList<>();
        try {
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(DATA_IMPORT_JOB_NAME, 0, MAX_JOB_INSTANCES_COUNT);
            if(jobInstances != null){
                for(JobInstance jobInstance : jobInstances){
                    Comparator<JobExecution> comparator = (e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime());
                    JobExecution lastJobExecution = jobExplorer.getJobExecutions(jobInstance).stream().max(comparator).get();
                    DataImportResult dataImportResult = new DataImportResult();
                    dataImportResult.setJobInstanceId(jobInstance.getId());
                    dataImportResult.setJobExecutionId(lastJobExecution.getId());
                    dataImportResult.setStatus(lastJobExecution.getStatus().toString());
                    List<String> runningSteps = new ArrayList<>();
                    if(lastJobExecution.isRunning()) {
                        lastJobExecution.getStepExecutions().stream()
                                .filter(s -> s.getStatus().equals(BatchStatus.STARTED)
                                        || s.getStatus().equals(BatchStatus.STARTING))
                                .forEach(s -> runningSteps.add(s.getStepName()));
                    }
                    dataImportResult.setRunningSteps(runningSteps);
                    dataImportResults.add(dataImportResult);
                }

            }
        } catch (Exception e) {
            log.error("Failed list import operations", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResults).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "stop data import", response = DataImportResult.class)
    @Path("/stop/{executionId}")
    public Response stop(@NotNull @ApiParam(name="executionId", required = true)
                             @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = new DataImportResult();
        try {
            jobOperator.stop(executionId);
            JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
            dataImportResult.setJobInstanceId(jobExecution.getJobInstance().getId());
            dataImportResult.setJobExecutionId(executionId);
            dataImportResult.setStatus(BatchStatus.STOPPING.toString());
        } catch (Exception e) {
            log.error("Failed to stop data import", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "restart data import", response = DataImportResult.class)
    @Path("/restart/{executionId}")
    public Response restart(@NotNull @ApiParam(name="executionId", required = true)
                                @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = new DataImportResult();
        try {
            Long newExecutionId = jobOperator.restart(executionId);
            JobExecution jobExecution = jobExplorer.getJobExecution(newExecutionId);
            dataImportResult.setJobInstanceId(jobExecution.getJobInstance().getId());
            dataImportResult.setJobExecutionId(newExecutionId);
            dataImportResult.setStatus(BatchStatus.STARTING.toString());
        } catch (Exception e) {
            log.error("Failed to restart data import", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "allow progress", response = DataImportResult.class)
    @Path("/allow-progress/{executionId}")
    public Response allowProgress(@NotNull @ApiParam(name="executionId", required = true)
                            @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = new DataImportResult();
        try {
            JobExecution jobExecution = jobExplorer.getJobExecution(executionId);

            if(!jobExecution.isRunning()){
                throw new WebApplicationException("The job execution is not running",
                        Response.Status.BAD_REQUEST);
            }
            DataImportRequest dataImportRequest =
                    (DataImportRequest)jobExecution.getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
            if(!dataImportRequest.isForceControlledProgress()){
                throw new WebApplicationException("The job execution is not controlled",
                        Response.Status.BAD_REQUEST);
            }

            String jobKey = jobExecution.getJobParameters().getString(CUSTOM_PARAMETERS_JOB_KEY);
            jobProgressManager.provideProgressPermission(jobKey);

            dataImportResult.setJobInstanceId(jobExecution.getJobInstance().getId());
            dataImportResult.setJobExecutionId(executionId);
            List<String> runningSteps = new ArrayList<>();
            jobExecution.getStepExecutions().stream()
                    .filter(s -> s.getStatus().equals(BatchStatus.STARTED)
                            || s.getStatus().equals(BatchStatus.STARTING))
                    .forEach(s -> runningSteps.add(s.getStepName()));
            dataImportResult.setRunningSteps(runningSteps);
            dataImportResult.setStatus(BatchStatus.STARTED.toString());
        } catch (Exception e) {
            log.error("Failed to allow progress", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    private void updateJobParameterRegistry(String jobParameterValue, DataImportRequest dataImportRequest){
        jobParametersRegistry.addParameter(jobParameterValue, dataImportRequest);
    }

    private String getJobParameterValue(){
        return UUID.randomUUID().toString();
    }

    private String getJobParametersStr(String jobParameterValue){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CUSTOM_PARAMETERS_JOB_KEY);
        stringBuilder.append("=");
        stringBuilder.append(jobParameterValue);
        return stringBuilder.toString();
    }

}
