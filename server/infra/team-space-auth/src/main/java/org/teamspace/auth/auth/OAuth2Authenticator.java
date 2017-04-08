package org.teamspace.auth.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.auth.dao.TokenProviderDao;
import org.teamspace.auth.domain.AccessToken;
import org.teamspace.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class OAuth2Authenticator implements Authenticator<String, User> {

	@Autowired
	private TokenProviderDao accessTokenDAO;

	public static final int ACCESS_TOKEN_EXPIRE_TIME_MIN = 30;
	

	@Override
	public Optional<User> authenticate(String accessTokenId) throws AuthenticationException {
		UUID accessTokenUUID;
		try {
			accessTokenUUID = UUID.fromString(accessTokenId);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}

		Optional<AccessToken> accessToken = accessTokenDAO.findAccessTokenById(accessTokenUUID);
		if (accessToken == null || !accessToken.isPresent()) {
			return Optional.empty();
		}

		Period period = new Period(accessToken.get().getLastAccessUTC(), new DateTime());
		if (period.getMinutes() > ACCESS_TOKEN_EXPIRE_TIME_MIN) {
			return Optional.empty();
		}

		accessTokenDAO.setLastAccessTime(accessTokenUUID, new DateTime());
		SecurityContext.setContext(accessToken.get());


		return Optional.of(accessToken.get().getUser());
	}




	
}
