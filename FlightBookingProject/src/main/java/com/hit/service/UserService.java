package com.hit.service;

import com.hit.dm.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final Map<String, User> usersById;
    private final Map<String, User> usersByUsername;

    public UserService() {
        this.usersById = new ConcurrentHashMap<>();
        this.usersByUsername = new ConcurrentHashMap<>();
    }

    public User register(String username, String password, String role) {
        if (usersByUsername.containsKey(username)) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        User user = new User(id, username, password, role != null ? role : "USER");
        usersById.put(id, user);
        usersByUsername.put(username, user);
        return user;
    }

    public User login(String username, String password) {
        User user = usersByUsername.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User findById(String id) {
        return usersById.get(id);
    }
}
