package org.teamspace.users.resources;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.teamspace.auth.domain.User;
import org.teamspace.users.service.UsersService;

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
@Api(value = "User",
        authorizations = {@Authorization("team_space_auth")})
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Component
@Slf4j
public class UserResource {

    @Autowired
    private UsersService usersService;

    @GET
    @ApiOperation(value = "find all users",
            response = User.class, responseContainer = "list")
    public Response findAll() {
        List<User> usersList = null;
        try {
            usersList = usersService.findAll();
        } catch (Exception e){
            String errMsg = "Unexpected error occurred when getting all users";
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(usersList).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "find user",
            response = User.class)
    public Response findOne(@NotNull @ApiParam(name="id", required = true)
                            @PathParam("id") Integer id) {
        User user = findUserById(id);
        return Response.ok(user).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create user", response = User.class)
    public Response create(@ApiParam(name = "user", required = true) User user) {
        if(!isValidUser(user)){
            throw new WebApplicationException("User properties can't be empty",
                    Response.Status.BAD_REQUEST);
        }
        User createdUser = null;
        try {
            createdUser = usersService.create(user);
        } catch (DuplicateKeyException e){
            String errMsg = String.format("User with username: %s already exists", user.getUsername());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when creating user: %s", user.getUsername());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update user", response = User.class)
    public Response update(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id, @ApiParam(name = "user", required = true) User user) {
        if(!isValidUser(user)){
            throw new WebApplicationException("User properties can't be empty",
                    Response.Status.BAD_REQUEST);
        }
        findUserById(id);
        user.setId(id);
        User updatedUser = null;
        try {
            usersService.update(user);
        } catch (DuplicateKeyException e){
            String errMsg = String.format("User with username: %s already exists", user.getUsername());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.BAD_REQUEST);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when creating user: %s", user.getUsername());
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.OK).entity(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete user", response = Response.class)
    public Response delete(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id) {
        User user = findUserById(id);
        try {
            usersService.delete(user);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when deleting user %s", id);
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete non privileged users", response = Response.class)
    public Response deleteNonPrevilegedUsers() {
        try {
            usersService.deleteNonAdminUsers();
        } catch (Exception e){
            String errMsg = "Unexpected error occurred when deleting all non privileged users";
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/current")
    @PermitAll
    @ApiOperation(value = "get current user",
            response = User.class)
    public Response getCurrentUser(@Auth User user) {
        return Response.status(Response.Status.OK).entity(user).build();
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create users", response = User.class, responseContainer = "list")
    public Response importUsers(@ApiParam(name = "user", required = true) List<User> users) {
        List<User> createdUsersList = null;
        try {
            createdUsersList = usersService.importUsers(users);
        } catch (Exception e){
            String errMsg = "Unexpected error occurred when importing users";
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.CREATED).entity(createdUsersList).build();
    }

    private boolean isValidUser(User user){
        return StringUtils.isNotBlank(user.getUsername())
                && StringUtils.isNotBlank(user.getPassword())
                && StringUtils.isNotBlank(user.getFirstName())
                && StringUtils.isNotBlank(user.getLastName())
                && StringUtils.isNotBlank(user.getRole());
    }

    private User findUserById(Integer id){
        User user = null;
        try {
            user = usersService.findOne(id);
        } catch (Exception e){
            String errMsg = String.format("Unexpected error occurred when getting user %s", id);
            log.error(errMsg, e);
            throw new WebApplicationException(errMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if(user == null){
            throw new WebApplicationException("User with ID " + id + " wasn't found", Response.Status.NOT_FOUND);
        }
        return user;
    }

}
