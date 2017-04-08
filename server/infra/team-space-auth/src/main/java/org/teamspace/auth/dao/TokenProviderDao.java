package org.teamspace.auth.dao;

import org.joda.time.DateTime;
import org.teamspace.auth.domain.AccessToken;
import org.teamspace.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface TokenProviderDao {
	public AccessToken generateNewAccessToken(final User user, final DateTime dateTime);
	public Optional<AccessToken> findAccessTokenById(final UUID accessTokenId);
	public void setLastAccessTime(final UUID accessTokenUUID, final DateTime dateTime);
}
