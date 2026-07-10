package com.hit.util;

import com.hit.controller.BookingController;
import com.hit.controller.Controller;
import com.hit.controller.FlightController;
import com.hit.controller.UserController;
import com.hit.dao.DaoFileImpl;
import com.hit.dao.IDao;
import com.hit.dm.FlightTicket;
import com.hit.dm.User;
import com.hit.algorithm.DijkstraAlgoImpl;
import com.hit.algorithm.IAlgoShortestPath;
import com.hit.service.FlightBookingService;
import com.hit.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class ControllerFactory {

    private static final String DEFAULT_DATA_SOURCE = "src/main/resources/datasource.txt";
    private static final String DEFAULT_USER_SOURCE = "src/main/resources/users.txt";
    private final Map<String, Controller> controllers;

    public ControllerFactory() {
        this.controllers = new HashMap<>();
        initializeControllers();
    }

    private String resolveDataSourcePath() {
        String path = System.getProperty("datasource.path");
        if (path != null) return path;

        String[] candidates = {
            DEFAULT_DATA_SOURCE,
            "../" + DEFAULT_DATA_SOURCE,
            "CommunicationLayer/" + DEFAULT_DATA_SOURCE,
            "../CommunicationLayer/" + DEFAULT_DATA_SOURCE
        };
        for (String candidate : candidates) {
            if (new java.io.File(candidate).exists()) {
                return candidate;
            }
        }
        return DEFAULT_DATA_SOURCE;
    }

    private String resolveUserSourcePath() {
        String path = System.getProperty("usersource.path");
        if (path != null) return path;

        String[] candidates = {
            DEFAULT_USER_SOURCE,
            "../" + DEFAULT_USER_SOURCE,
            "CommunicationLayer/" + DEFAULT_USER_SOURCE,
            "../CommunicationLayer/" + DEFAULT_USER_SOURCE
        };
        for (String candidate : candidates) {
            if (new java.io.File(candidate).exists()) {
                return candidate;
            }
        }
        return DEFAULT_USER_SOURCE;
    }

    private void initializeControllers() {
        IDao<FlightTicket> dao = new DaoFileImpl<>(resolveDataSourcePath());
        IDao<User> userDao = new DaoFileImpl<>(resolveUserSourcePath());
        IAlgoShortestPath algo = new DijkstraAlgoImpl();
        FlightBookingService flightBookingService = new FlightBookingService(dao, algo);
        UserService userService = new UserService(userDao, algo);

        controllers.put("flight", new FlightController(flightBookingService));
        controllers.put("booking", new BookingController(flightBookingService));
        controllers.put("user", new UserController(userService));
    }

    public Controller getController(String key) {
        return controllers.get(key);
    }
}
