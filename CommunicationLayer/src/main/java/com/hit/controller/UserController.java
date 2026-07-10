package com.hit.controller;

import com.google.gson.JsonObject;
import com.hit.dm.User;
import com.hit.server.Request;
import com.hit.server.Response;
import com.hit.service.UserService;

public class UserController implements Controller {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response<?> handle(Request<?> request) {
        String action = request.getHeaders().get("action");
        String subAction = action.contains("/") ? action.split("/", 2)[1] : "";

        switch (subAction) {
            case "register": {
                JsonObject body = (JsonObject) request.getBody();
                if (body == null || !body.has("username") || !body.has("password")) {
                    return new Response<>(400, "Missing username or password", null);
                }
                String username = body.get("username").getAsString();
                String password = body.get("password").getAsString();
                String role = body.has("role") ? body.get("role").getAsString() : "USER";

                User user = userService.register(username, password, role);
                if (user == null) {
                    return new Response<>(409, "Username already exists: " + username, null);
                }
                return new Response<>(201, "User registered", sanitizeUser(user));
            }
            case "login": {
                JsonObject body = (JsonObject) request.getBody();
                if (body == null || !body.has("username") || !body.has("password")) {
                    return new Response<>(400, "Missing username or password", null);
                }
                String username = body.get("username").getAsString();
                String password = body.get("password").getAsString();

                User user = userService.login(username, password);
                if (user == null) {
                    return new Response<>(401, "Invalid username or password", null);
                }
                return new Response<>(200, "Login successful", sanitizeUser(user));
            }
            default:
                return new Response<>(404, "Unknown user action: " + action, null);
        }
    }

    private User sanitizeUser(User user) {
        return new User(user.getId(), user.getUsername(), null, user.getRole());
    }
}
