package org.teamspace.membership.resources;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.membership.domain.Membership;
import org.teamspace.membership.service.MembershipsService;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.status;

/**
 * Created by shpilb on 21/01/2017.
 */
@Api(value = "Membership",
        authorizations = {@Authorization("team_space_auth")})
@Path("/memberships")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Component
public class MembershipsResource {

    @Autowired
    private MembershipsService membershipsService;

    @GET
    @ApiOperation(value = "find all memberships",
            response = Membership.class, responseContainer = "list")
    public List<Membership> findAll() {
        return membershipsService.findAll();
    }

    @GET
    @Path("/raw")
    @ApiOperation(value = "find all raw memberships",
            response = Membership.class, responseContainer = "list")
    public List<Membership> findAllRaw() {
        return membershipsService.findAllRaw();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "find membership",
            response = Membership.class)
    public Membership findOne(@NotNull @ApiParam(name="id", required = true)
                            @PathParam("id") Integer id) {
        Membership membership = findMembershipById(id);
        return membership;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create membership", response = Membership.class)
    public Response create(@ApiParam(name = "Membership", required = true) Membership membership) {
        Membership createdMembership = membershipsService.create(membership);
        return Response.status(Response.Status.CREATED).entity(createdMembership).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete membership", response = Response.class)
    public Response delete(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id) {
        Membership membership = findMembershipById(id);
        membershipsService.delete(membership);
        return status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete all membership", response = Response.class)
    public Response deleteAll() {
        membershipsService.deleteAllMemberships();
        return status(Response.Status.NO_CONTENT).build();
    }


    private Membership findMembershipById(Integer id){
        Membership membership = membershipsService.findOne(id);
        if(membership == null){
            throw new WebApplicationException("membership with ID " + id + " wasn't found ", Response.Status.NOT_FOUND);
        }
        return membership;
    }

}
