package org.teamspace.auth.dao.impl;

import com.google.common.base.Optional;
import org.springframework.stereotype.Repository;
import org.teamspace.auth.dao.UsersLocatorDao;
import org.teamspace.auth.domain.User;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UsersLocatorDaoImpl implements UsersLocatorDao{
	
	final Map<Integer, User> usersMap = new HashMap<>();

	@PostConstruct
	private void init(){
		usersMap.put(1, new User(1, "user1", "pass1"));
		usersMap.put(2, new User(2, "user2", "pass2"));
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
