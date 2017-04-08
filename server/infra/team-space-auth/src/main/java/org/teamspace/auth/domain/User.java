package org.teamspace.auth.domain;

import lombok.*;

import java.security.Principal;

@Data
@AllArgsConstructor
public class User implements Principal {
	private Integer id;
	private String username;
	private String password;

	
	@Override
	public String getName() {
		return username;
	}	
	
}
