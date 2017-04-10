package org.teamspace.auth.dao.impl;

import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.teamspace.auth.dao.UsersLocatorDao;
import org.teamspace.auth.domain.User;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UsersLocatorDaoImpl implements UsersLocatorDao{
	
	final Map<Integer, User> usersMap = new HashMap<>();

	@Value("${adminCredentials.user}")
	private String adminUser;

	@Value("${adminCredentials.password}")
	private String adminPassword;


	@PostConstruct
	private void init(){
		usersMap.put(1, new User(1, adminUser, adminPassword));
	}

	public Optional<User> findUserByUsernameAndPassword(final String username, final String password) {
		for (Map.Entry<Integer, User> entry : usersMap.entrySet()) {
			User user = entry.getValue();
			if (user.getPassword().equals(password) && user.getUsername().equals(username)) {
				return Optional.of(user);
			}
		}
		return Optional.absent();
	}
}
