package org.teamspace.auth.dao;

import org.joda.time.DateTime;
import org.teamspace.auth.domain.AccessToken;
import org.teamspace.auth.domain.User;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TokenProviderDAO {
	
	private static Map<UUID, AccessToken> tokensMap = new HashMap<UUID, AccessToken>();
	
	public AccessToken generateNewAccessToken(final User user, final DateTime dateTime) {
		AccessToken accessToken = new AccessToken(UUID.randomUUID(), dateTime, user);
		tokensMap.put(accessToken.getAccessTokenId(), accessToken);
		return accessToken;
	}
	//comment #1
	public Optional<AccessToken> findAccessTokenById(final UUID accessTokenId) {
		AccessToken accessToken = tokensMap.get(accessTokenId);
		if (accessToken == null) {
			return Optional.empty();
		}
		return Optional.of(accessToken);
	}


	public void setLastAccessTime(final UUID accessTokenUUID, final DateTime dateTime) {
		AccessToken accessToken = tokensMap.get(accessTokenUUID);
		AccessToken updatedAccessToken = accessToken.withLastAccessUTC(dateTime);
		tokensMap.put(accessTokenUUID, updatedAccessToken);
	}
}
