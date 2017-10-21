package org.teamspace.data_import.resources;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.domain.DataImportResult;
import org.teamspace.data_import.job_manager.DataImportJobManager;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by shpilb on 21/01/2017.
 */
@Api(value = "Data Import",
        authorizations = {@Authorization("team_space_auth")})
@Path("/data-import")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Component
@Slf4j
public class DataImportResource {

    @Autowired
    private DataImportJobManager dataImportJobManager;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "initiate data import", response = DataImportResult.class)
    public Response create(@ApiParam(name = "dataImportRequest", required = true) DataImportRequest dataImportRequest) {
        DataImportResult dataImportResult = null;
        try {
            dataImportResult = dataImportJobManager.create(dataImportRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.CREATED).entity(dataImportResult).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get data import operations", response = DataImportResult.class, responseContainer = "List")
    public Response findAll() {
        List<DataImportResult> dataImportResults = null;
        try {
            dataImportResults = dataImportJobManager.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.OK).entity(dataImportResults).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "stop data import", response = DataImportResult.class)
    @Path("/stop/{executionId}")
    public Response stop(@NotNull @ApiParam(name="executionId", required = true)
                             @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = null;
        try {
            dataImportResult = dataImportJobManager.stop(executionId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "restart data import", response = DataImportResult.class)
    @Path("/restart/{executionId}")
    public Response restart(@NotNull @ApiParam(name="executionId", required = true)
                                @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = null;
        try {
            dataImportResult = dataImportJobManager.restart(executionId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "allow progress", response = DataImportResult.class)
    @Path("/allow-progress/{executionId}")
    public Response allowProgress(@NotNull @ApiParam(name="executionId", required = true)
                            @PathParam("executionId") Long executionId) {
        DataImportResult dataImportResult = null;
        try {
            dataImportResult = dataImportJobManager.allowProgress(executionId);
        } catch (WebApplicationException e){
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.OK).entity(dataImportResult).build();
    }
}
