package org.teamspace.users.resources;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.springframework.stereotype.Component;
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
@Component
public class UserResource {


    @GET
    @Path("/current")
    @PermitAll
    @ApiOperation(value = "get current user",
            response = User.class)
    public User getCurrentUser(@Auth User user) {
        return user;
    }

}
