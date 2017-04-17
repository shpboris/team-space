package org.teamspace.users.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.teamspace.auth.domain.User;
import org.teamspace.users.dao.UsersDao;
import org.teamspace.users.service.impl.UserServiceImpl;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by shpilb on 17/04/2017.
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userServiceImpl;
    @Mock
    private UsersDao usersDao;

    @Before
    public void setup() {
        try {
            when(usersDao.findAll()).thenReturn(getUsersList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindNonPrivilegedUsers(){
        List<User> nonPrivilegedUsers = userServiceImpl.findNonPrivilegedUsers();
        assertEquals(nonPrivilegedUsers.size(), 1);
        assertEquals(nonPrivilegedUsers.get(0).getFirstName(), "f1");
    }

    private List<User> getUsersList(){
        User user1 = new User("u1", "p1", "f1", "l1", "USERS");
        User user2 = new User("u2", "p2", "f2", "l2", "ADMIN");
        return Arrays.asList(user1, user2);
    }

}
