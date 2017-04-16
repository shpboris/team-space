package org.teamspace.users.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teamspace.auth.domain.User;
import org.teamspace.users.dao.UsersDao;
import org.teamspace.users.service.UsersService;

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


    public void init(){
        User admin = usersDao.findUserById(1);
        if(admin == null){
            admin = getAdminUser();
            usersDao.insertUser(admin);
        }

    }

    @Override
    public void insertUser(User user) {
        usersDao.insertUser(user);
    }

    private User getAdminUser(){
        User admin = new User(1, adminUser, adminPassword);
        return admin;
    }
}
