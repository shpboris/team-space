package org.teamspace.membership.resources;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
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
@Slf4j
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
        if(!isValidMembership(membership)){
            throw new WebApplicationException("Membership should have not empty references to group and user",
                    Response.Status.BAD_REQUEST);
        }
        Membership createdMembership = null;
        try {
            createdMembership = membershipsService.create(membership);
        } catch (DuplicateKeyException e){
            String errMsg = String.format("Membership for user: %s and group: %s already exists",
                    membership.getUser().getId(), membership.getGroup().getId());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        } catch (DataIntegrityViolationException e){
            String errMsg = String.format("Membership should point to valid existing user and group");
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when creating membership" +
                            " for user: %s and group: %s",
                    membership.getUser().getId(), membership.getGroup().getId());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
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
            throw new WebApplicationException("Membership with ID " + id + " wasn't found", Response.Status.NOT_FOUND);
        }
        return membership;
    }

    private boolean isValidMembership(Membership membership){
        return membership != null
                && membership.getUser() != null
                && membership.getGroup() != null
                && membership.getUser().getId() != null
                && membership.getGroup().getId() != null;
    }

}
