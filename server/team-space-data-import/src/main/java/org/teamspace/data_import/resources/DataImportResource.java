package org.teamspace.data_import.resources;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.domain.DataImportResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private JobLauncher jobLauncher;

    @Autowired
    private Job dataImportJob;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "import data", response = DataImportResult.class)
    public Response create(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest user) {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(dataImportJob, jobParameters);
        } catch (Exception e) {
            log.error("Failed to import data", e);
        }
        DataImportResult dataImportResult = new DataImportResult();
        return Response.status(Response.Status.CREATED).entity(dataImportResult).build();
    }


}
