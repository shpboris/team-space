package org.teamspace.users.resources;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserResource {

    @Autowired
    private UsersService usersService;

    @GET
    @ApiOperation(value = "find all users",
            response = User.class, responseContainer = "list")
    public List<User> findAll() {
        return usersService.findAll();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "find user",
            response = User.class)
    public User findOne(@NotNull @ApiParam(name="id", required = true)
                            @PathParam("id") Integer id) {
        User user = findUserById(id);
        return user;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create user", response = User.class)
    public Response create(@ApiParam(name = "user", required = true) User user) {
        User createdUser = usersService.create(user);
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update user", response = User.class)
    public Response update(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id, @ApiParam(name = "user", required = true) User user) {
        findUserById(id);
        user.setId(id);
        User updatedUser = usersService.update(user);
        return Response.status(Response.Status.OK).entity(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete user", response = Response.class)
    public Response delete(@NotNull @ApiParam(name="id", required = true)
                               @PathParam("id") Integer id) {
        User user = findUserById(id);
        usersService.delete(user);
        return status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/current")
    @PermitAll
    @ApiOperation(value = "get current user",
            response = User.class)
    public User getCurrentUser(@Auth User user) {
        return user;
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create users", response = User.class, responseContainer = "list")
    public Response importUsers(@ApiParam(name = "user", required = true) List<User> users) {
        List<User> createdUsersList = usersService.importUsers(users);
        return Response.status(Response.Status.CREATED).entity(createdUsersList).build();
    }


    private User findUserById(Integer id){
        User user = usersService.findOne(id);
        if(user == null){
            throw new WebApplicationException("User with ID " + id + " wasn't found ", Response.Status.NOT_FOUND);
        }
        return user;
    }

}
