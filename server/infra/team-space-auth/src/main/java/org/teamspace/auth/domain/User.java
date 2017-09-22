package org.teamspace.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.security.Principal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Principal {
	private Integer id;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String role;

	public User(String username, String password, String firstName, String lastName, String role){
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
	}

	
	@Override
	@JsonIgnore
	public String getName() {
		return username;
	}	
	
}
