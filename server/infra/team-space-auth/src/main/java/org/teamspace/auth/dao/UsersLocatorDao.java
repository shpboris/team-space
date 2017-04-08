package org.teamspace.auth.dao;

import com.google.common.base.Optional;
import org.teamspace.auth.domain.User;


public interface UsersLocatorDao {
	public Optional<User> findUserByUsernameAndPassword(final String username, final String password);
}
