package org.teamspace.auth.resources;

import com.google.common.base.Optional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.auth.dao.TokenProviderDao;
import org.teamspace.auth.dao.UsersLocatorDao;
import org.teamspace.auth.domain.AccessToken;
import org.teamspace.auth.domain.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Path("/oauth2/token")
@Produces(MediaType.APPLICATION_JSON)
@Api("Authentication")
@Component
public class TokenProviderResource {

	@Autowired
	private UsersLocatorDao userDAO;
	@Autowired
	private TokenProviderDao accessTokenDAO;


	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "acquire token",
			response = Map.class)
	public Map<String, String> postForToken(
			@FormParam("grant_type") String grantType,
			@FormParam("username") String username,
			@FormParam("password") String password)
	{

		
		Optional<User> user = userDAO.findUserByUsernameAndPassword(username, password);
		if (user == null || !user.isPresent()) {
			throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
		}

		AccessToken accessToken = accessTokenDAO.generateNewAccessToken(user.get(), new DateTime());
		Map<String, String> response = new HashMap<String, String>();
		response.put("token_type", "Bearer");
		response.put("access_token", accessToken.getAccessTokenId().toString());
		return response;
	}


}
