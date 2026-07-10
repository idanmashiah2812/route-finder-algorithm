package com.hit.service;

import com.hit.algorithm.DijkstraAlgoImpl;
import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.DaoFileImpl;
import com.hit.dm.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class UserServiceTest {

    private static final String TEST_USER_FILE = "src/test/resources/test_users.txt";
    private UserService userService;

    @Before
    public void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_USER_FILE));

        DaoFileImpl<User> userDao = new DaoFileImpl<>(TEST_USER_FILE);
        IAlgoShortestPath algo = new DijkstraAlgoImpl();
        userService = new UserService(userDao, algo);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_USER_FILE));
    }

    @Test
    public void testRegisterAndFindUser() {
        User registered = userService.register("alice", "pass123", "USER");
        assertNotNull(registered);
        assertNotNull(registered.getId());
        assertEquals("alice", registered.getUsername());
        assertEquals("pass123", registered.getPassword());
        assertEquals("USER", registered.getRole());

        User found = userService.findById(registered.getId());
        assertNotNull(found);
        assertEquals("alice", found.getUsername());
    }

    @Test
    public void testRegisterDuplicateUsername() {
        assertNotNull(userService.register("bob", "pass1", "USER"));
        assertNull(userService.register("bob", "pass2", "USER"));
    }

    @Test
    public void testLoginSuccess() {
        userService.register("charlie", "secret", "ADMIN");
        User loggedIn = userService.login("charlie", "secret");
        assertNotNull(loggedIn);
        assertEquals("charlie", loggedIn.getUsername());
        assertEquals("ADMIN", loggedIn.getRole());
    }

    @Test
    public void testLoginWrongPassword() {
        userService.register("dave", "correct", "USER");
        assertNull(userService.login("dave", "wrong"));
    }

    @Test
    public void testDeleteUser() {
        User user = userService.register("eve", "pwd", "USER");
        assertNotNull(user);
        String id = user.getId();
        assertNotNull(userService.findById(id));
        userService.deleteUser(id);
        assertNull(userService.findById(id));
    }

    @Test
    public void testPersistenceAcrossInstances() {
        userService.register("frank", "mypass", "USER");

        DaoFileImpl<User> secondDao = new DaoFileImpl<>(TEST_USER_FILE);
        IAlgoShortestPath algo = new DijkstraAlgoImpl();
        UserService secondService = new UserService(secondDao, algo);

        User loaded = secondService.login("frank", "mypass");
        assertNotNull(loaded);
        assertEquals("frank", loaded.getUsername());
    }
}
