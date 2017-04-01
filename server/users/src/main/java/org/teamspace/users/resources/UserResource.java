package org.teamspace.users.resources;

import io.swagger.annotations.*;
import org.teamspace.auth.domain.User;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by shpilb on 21/01/2017.
 */
@Api(value = "User",
        authorizations = {@Authorization("team_space_auth")})
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class UserResource {


    @GET
    @Path("/current")
    @PermitAll
    @ApiOperation(value = "get current user",
            response = User.class)
    public User getCurrentUser() {
        int x = 0;
        return new User(1, "user1", "pass1");
    }


    @GET
    @Path("/another")
    @PermitAll
    @ApiOperation(value = "get another user", response = User.class)
    public User getAnotherUser() {
        int x = 0;
        return new User(2, "user2", "pass2");
    }

}
