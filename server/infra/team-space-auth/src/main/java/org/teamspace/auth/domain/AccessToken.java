package org.teamspace.auth.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Wither
public class AccessToken {
	@JsonProperty("access_token_id")
	@NotNull
	private UUID accessTokenId;

	@JsonProperty("last_access_utc")
	@NotNull
	private DateTime lastAccessUTC;
	
	@JsonProperty("user")
	@NotNull
	private User user;
}
