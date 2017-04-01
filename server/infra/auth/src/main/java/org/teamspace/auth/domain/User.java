package org.teamspace.auth.domain;

import lombok.*;

import java.security.Principal;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class User implements Principal {
	private Integer id;
	private String username;
	private String password;

	
	@Override
	public String getName() {
		return username;
	}	
	
}
