package org.teamspace.users.service;

import org.teamspace.auth.domain.User;

import java.util.List;

/**
 * Created by shpilb on 11/04/2017.
 */
public interface UsersService {
    public List<User> findAll();
    public User findOne(Integer id);
    public User create(User user);
    public User update(User user);
    public void delete(User user);
    public void init();
}
