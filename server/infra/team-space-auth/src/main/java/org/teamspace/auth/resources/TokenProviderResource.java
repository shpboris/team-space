package org.teamspace.auth.resources;

import com.google.common.base.Optional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.teamspace.auth.dao.TokenProviderDAO;
import org.teamspace.auth.dao.UserDAO;
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
public class TokenProviderResource {
	

	private UserDAO userDAO;
	private TokenProviderDAO accessTokenDAO;
	

	public TokenProviderResource() {
		userDAO = new UserDAO();
		accessTokenDAO = new TokenProviderDAO();
	}


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
