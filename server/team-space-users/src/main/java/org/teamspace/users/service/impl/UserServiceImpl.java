package org.teamspace.users.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.auth.domain.User;
import org.teamspace.users.dao.UsersDao;
import org.teamspace.users.service.UsersService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shpilb on 11/04/2017.
 */
@Service
public class UserServiceImpl implements UsersService {

    @Autowired
    private UsersDao usersDao;

    @Value("${adminCredentials.user}")
    private String adminUser;

    @Value("${adminCredentials.password}")
    private String adminPassword;

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USERS";


    public void init(){
        User admin = usersDao.findOneByUsername(adminUser);
        if(admin == null){
            admin = getAdminUser();
            usersDao.create(admin);
        }

    }

    @Override
    public List<User> findAll() {
        return usersDao.findAll();
    }

    @Override
    public User findOne(Integer id) {
        return usersDao.findOne(id);
    }

    @Override
    public User create(User user) {
        usersDao.create(user);
        return usersDao.findOneByUsername(user.getUsername());
    }

    @Override
    public User update(User user) {
        usersDao.update(user);
        return usersDao.findOneByUsername(user.getUsername());
    }

    @Override
    public void delete(User user) {
        usersDao.delete(user);
    }


    @Override
    public List<User> findNonPrivilegedUsers() {
        return usersDao.findAll().stream().filter(u -> u.getRole().equals(USER_ROLE)).collect(Collectors.toList());
    }

    private User getAdminUser(){
        User admin = new User(adminUser, adminPassword, adminUser, adminUser, ADMIN_ROLE);
        return admin;
    }
}
