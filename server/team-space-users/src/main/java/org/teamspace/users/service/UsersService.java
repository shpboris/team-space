package org.teamspace.users.service;

import org.teamspace.auth.domain.User;

/**
 * Created by shpilb on 11/04/2017.
 */
public interface UsersService {
    public void init();
    void insertUser(User user);
}
