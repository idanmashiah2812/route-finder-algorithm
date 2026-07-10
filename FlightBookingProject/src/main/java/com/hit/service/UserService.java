package com.hit.service;

import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.IDao;
import com.hit.dm.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final IDao<User> dao;
    @SuppressWarnings("unused")
    private final IAlgoShortestPath algo;
    private final Map<String, User> usersById;
    private final Map<String, User> usersByUsername;

    public UserService(IDao<User> dao, IAlgoShortestPath algo) {
        this.dao = dao;
        this.algo = algo;
        this.usersById = new ConcurrentHashMap<>();
        this.usersByUsername = new ConcurrentHashMap<>();
        loadFromDao();
    }

    private void loadFromDao() {
        List<User> allUsers = dao.findAll();
        for (User user : allUsers) {
            usersById.put(user.getId(), user);
            usersByUsername.put(user.getUsername(), user);
        }
    }

    public User register(String username, String password, String role) {
        if (usersByUsername.containsKey(username)) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        User user = new User(id, username, password, role != null ? role : "USER");
        usersById.put(id, user);
        usersByUsername.put(username, user);
        dao.save(user);
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

    public void deleteUser(String id) {
        User user = usersById.remove(id);
        if (user != null) {
            usersByUsername.remove(user.getUsername());
            dao.delete(id);
        }
    }
}
