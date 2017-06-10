package org.teamspace.users.service.module_test;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.teamspace.auth.domain.User;
import org.teamspace.users.service.UsersService;
import persistence_test.common.BaseModuleTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shpilb on 17/04/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UsersPersistenceConfig.class)
@Slf4j
public class UserServiceModuleTest extends BaseModuleTest{

    @Autowired
    private UsersService usersService;

    @Before
    public void setup() {
        super.setup();
        usersService.init();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testE2eScenario(){

        //create two users
        List<User> origUsers = getUsersList();
        usersService.create(origUsers.get(0));
        usersService.create(origUsers.get(1));
        List<User> users = usersService.findAll();
        assertEquals(users.size(), 3);
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("u1")));
        assertTrue(users.stream().anyMatch(u -> u.getRole().equals("ADMIN")));

        //update user
        User u1 = users.stream().filter(u -> u.getUsername().equals("u1")).findFirst().get();
        u1.setUsername("uu1");
        usersService.update(u1);
        u1 = usersService.findOne(u1.getId());
        assertEquals(u1.getUsername(), "uu1");
        assertEquals(usersService.findAll().size(), 3);

        //delete user
        usersService.delete(u1);
        users = usersService.findAll();
        assertEquals(users.size(), 2);
        assertFalse(users.stream().filter(u -> u.getUsername().equals("u1")).findAny().isPresent());

    }

    private List<User> getUsersList(){
        User user1 = new User("u1", "p1", "f1", "l1", "USER");
        User user2 = new User("u2", "p2", "f2", "l2", "USER");
        return Arrays.asList(user1, user2);
    }

}
