package org.teamspace.auth.dao.impl;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.teamspace.auth.dao.TokenProviderDao;
import org.teamspace.auth.domain.AccessToken;
import org.teamspace.auth.domain.User;

import java.util.*;

@Repository
public class TokenProviderDaoImpl implements TokenProviderDao{
	
	private Map<UUID, AccessToken> tokensMap = new HashMap<UUID, AccessToken>();
	
	public AccessToken generateNewAccessToken(final User user, final DateTime dateTime) {
		AccessToken accessToken = new AccessToken(UUID.randomUUID(), dateTime, user);
		tokensMap.put(accessToken.getAccessTokenId(), accessToken);
		return accessToken;
	}

	public Optional<AccessToken> findAccessTokenById(final UUID accessTokenId) {
		AccessToken accessToken = tokensMap.get(accessTokenId);
		if (accessToken == null) {
			return Optional.empty();
		}
		return Optional.of(accessToken);
	}


	public void setLastAccessTime(final UUID accessTokenUUID, final DateTime dateTime) {
		AccessToken accessToken = tokensMap.get(accessTokenUUID);
		accessToken.setLastAccessUTC(dateTime);
	}
}
