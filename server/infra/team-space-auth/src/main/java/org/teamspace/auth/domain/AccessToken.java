package org.teamspace.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AccessToken {
	private UUID accessTokenId;
	private DateTime lastAccessUTC;
	private User user;
}
