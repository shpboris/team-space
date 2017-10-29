package org.teamspace.groups.resources;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
@Slf4j
public class GroupsResource {

    @Autowired
    private GroupsService groupsService;

    @GET
    @ApiOperation(value = "find all groups",
            response = Group.class, responseContainer = "list")
    public Response findAll() {
        List<Group> groupList = null;
        try {
            groupList = groupsService.findAll();
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when getting all groups");
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(groupList).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "find group",
            response = Group.class)
    public Response findOne(@NotNull @ApiParam(name="id", required = true)
                            @PathParam("id") Integer id) {
        Group group = findGroupById(id);
        return Response.ok(group).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create group", response = Group.class)
    public Response create(@ApiParam(name = "group", required = true) Group group) {
        Group createdGroup = null;
        if(StringUtils.isBlank(group.getName())){
            throw new WebApplicationException("Group name can't be empty",
                    Response.Status.BAD_REQUEST);
        }
        try {
            createdGroup = groupsService.create(group);
        } catch (DuplicateKeyException e){
            String errMsg = String.format("Group with name: %s already exists", group.getName());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when creating group: %s", group.getName());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.CREATED).entity(createdGroup).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update group", response = Group.class)
    public Response update(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id, @ApiParam(name = "group", required = true) Group group) {
        if(StringUtils.isBlank(group.getName())){
            throw new WebApplicationException("Group name can't be empty",
                    Response.Status.BAD_REQUEST);
        }
        findGroupById(id);
        group.setId(id);
        Group updatedGroup = null;
        try {
            groupsService.update(group);
        } catch (DuplicateKeyException e){
            String errMsg = String.format("Group with name: %s already exists", group.getName());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).entity(updatedGroup).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete group", response = Response.class)
    public Response delete(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id) {
        Group group = findGroupById(id);
        try {
            groupsService.delete(group);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when deleting group %s", id);
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete all groups", response = Response.class)
    public Response deleteAll() {
        try {
            groupsService.deleteAllGroups();
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when deleting all groups");
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return status(Response.Status.NO_CONTENT).build();
    }


    private Group findGroupById(Integer id){
        Group group = null;
        try {
            group = groupsService.findOne(id);
        }
        catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when getting group %s", id);
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if(group == null){
            throw new WebApplicationException("Group with ID " + id + " wasn't found", Response.Status.NOT_FOUND);
        }
        return group;
    }

}
