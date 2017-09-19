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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "initiate data import", response = DataImportResult.class)
    public Response create(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest dataImportRequest) {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        DataImportResult dataImportResult = new DataImportResult();
        try {
            Long jobId = jobOperator.start(DATA_IMPORT_JOB_NAME, UUID.randomUUID().toString());
            dataImportResult.setJobId(jobId);
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
                    dataImportResult.setJobId(lastJobExecution.getId());
                    dataImportResult.setStatus(lastJobExecution.getStatus().toString());
                    if(lastJobExecution.isRunning()) {
                        lastJobExecution.getStepExecutions().stream()
                                .filter(s -> s.getStatus().equals(BatchStatus.STARTED)
                                        || s.getStatus().equals(BatchStatus.STARTING))
                                .forEach(s -> dataImportResult.setStep(s.getStepName()));
                    }
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
    @Path("/stop")
    public Response stop(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest dataImportRequest) {
        DataImportResult dataImportResult = new DataImportResult();
        try {
            jobOperator.stop(dataImportRequest.getJobId());
            dataImportResult.setStatus(BatchStatus.STOPPING.toString());
        } catch (Exception e) {
            log.error("Failed to stop data import", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "restart data import", response = DataImportResult.class)
    @Path("/restart")
    public Response restart(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest dataImportRequest) {
        DataImportResult dataImportResult = new DataImportResult();
        try {
            jobOperator.restart(dataImportRequest.getJobId());
            dataImportResult.setStatus(BatchStatus.STARTING.toString());
        } catch (Exception e) {
            log.error("Failed to restart data import", e);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

}
