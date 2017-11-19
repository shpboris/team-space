package org.teamspace.server_details.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.teamspace.server_details.domain.ServerDetails;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by shpilb on 21/01/2017.
 */
@Api(value = "Server details")
@Path("/server")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
public class ServerResource {

    private ServerDetails serverDetails;

    @GET
    @ApiOperation(value = "find server details",
            response = ServerDetails.class)
    public Response findOne() {
        if(serverDetails == null){
            serverDetails = new ServerDetails(UUID.randomUUID());
        }
        return Response.status(Response.Status.OK).entity(serverDetails).build();
    }


}
