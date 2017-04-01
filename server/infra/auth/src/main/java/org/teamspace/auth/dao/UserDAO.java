package org.teamspace.auth.dao;

import com.google.common.base.Optional;
import org.teamspace.auth.domain.User;

import java.util.HashMap;
import java.util.Map;


public class UserDAO {
	
	final static Map<Integer, User> usersMap = new HashMap<>();

	static {
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
