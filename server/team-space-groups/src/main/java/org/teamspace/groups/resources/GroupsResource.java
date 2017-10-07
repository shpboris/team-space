package org.teamspace.groups.resources;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.groups.domain.Group;
import org.teamspace.groups.service.GroupsService;

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
@Api(value = "Group",
        authorizations = {@Authorization("team_space_auth")})
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Component
public class GroupsResource {

    @Autowired
    private GroupsService groupsService;

    @GET
    @ApiOperation(value = "find all groups",
            response = Group.class, responseContainer = "list")
    public List<Group> findAll() {
        return groupsService.findAll();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "find group",
            response = Group.class)
    public Group findOne(@NotNull @ApiParam(name="id", required = true)
                            @PathParam("id") Integer id) {
        Group group = findGroupById(id);
        return group;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create group", response = Group.class)
    public Response create(@ApiParam(name = "group", required = true) Group group) {
        Group createdGroup = groupsService.create(group);
        return Response.status(Response.Status.CREATED).entity(createdGroup).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update group", response = Group.class)
    public Response update(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id, @ApiParam(name = "group", required = true) Group group) {
        findGroupById(id);
        group.setId(id);
        Group updatedGroup = groupsService.update(group);
        return Response.status(Response.Status.OK).entity(updatedGroup).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete group", response = Response.class)
    public Response delete(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id) {
        Group group = findGroupById(id);
        groupsService.delete(group);
        return status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete all groups", response = Response.class)
    public Response deleteAll() {
        groupsService.deleteAllGroups();
        return status(Response.Status.NO_CONTENT).build();
    }


    private Group findGroupById(Integer id){
        Group group = groupsService.findOne(id);
        if(group == null){
            throw new WebApplicationException("Group with ID " + id + " wasn't found ", Response.Status.NOT_FOUND);
        }
        return group;
    }

}
