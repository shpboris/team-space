package org.teamspace.users.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.auth.domain.User;
import org.teamspace.users.dao.UsersDao;
import org.teamspace.users.service.UsersService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

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
    public static final String USER_ROLE = "USER";


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
    @Transactional(TX_MANAGER)
    public User create(User user) {
        usersDao.create(user);
        return usersDao.findOneByUsername(user.getUsername());
    }

    @Override
    @Transactional(TX_MANAGER)
    public User update(User user) {
        usersDao.update(user);
        return usersDao.findOneByUsername(user.getUsername());
    }

    @Override
    @Transactional(TX_MANAGER)
    public void delete(User user) {
        usersDao.delete(user);
    }


    @Override
    public List<User> findNonPrivilegedUsers() {
        return usersDao.findAll().stream().filter(u -> u.getRole().equals(USER_ROLE)).collect(Collectors.toList());
    }

    @Override
    @Transactional(TX_MANAGER)
    public List<User> importUsers(List<User> users) {
        List<User> createdUsersList = new ArrayList<>();
        Map<String, User> existingUsers = findAll()
                .stream().collect(Collectors.toMap(User::getUsername, Function.identity()));
        for(User currUser : users){
            if(existingUsers.get(currUser.getUsername()) != null){
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
            User createdUser = create(currUser);
            createdUsersList.add(createdUser);
        }
        return createdUsersList;
    }

    @Override
    @Transactional(TX_MANAGER)
    public void deleteNonAdminUsers() {
        findAll().stream().filter(u -> !isAdminUser(u)).forEach(u -> {
            delete(u);
        });
    }

    private User getAdminUser(){
        User admin = new User(adminUser, adminPassword, adminUser, adminUser, ADMIN_ROLE);
        return admin;
    }

    private boolean isAdminUser(User user) {
        return user.getUsername().equals(adminUser);
    }

}
