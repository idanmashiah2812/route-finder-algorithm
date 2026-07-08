package com.hit.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hit.controller.Controller;
import com.hit.util.ControllerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class HandleRequest implements Runnable {

    private final Socket socket;
    private final ControllerFactory controllerFactory;
    private final Gson gson;

    public HandleRequest(Socket socket, ControllerFactory controllerFactory) {
        this.socket = socket;
        this.controllerFactory = controllerFactory;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(30000);
        } catch (IOException e) {
            System.err.println("Failed to set socket timeout: " + e.getMessage());
        }

        try (
                Scanner scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            StringBuilder jsonInput = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) {
                    break;
                }
                jsonInput.append(line);
            }

            if (jsonInput.length() == 0) {
                sendError(writer, 400, "Empty request");
                return;
            }

            JsonObject root = gson.fromJson(jsonInput.toString(), JsonObject.class);
            if (root == null || !root.has("headers")) {
                sendError(writer, 400, "Invalid request format");
                return;
            }

            JsonObject headers = root.getAsJsonObject("headers");
            JsonElement bodyElem = root.get("body");
            JsonObject body = (bodyElem instanceof JsonObject) ? bodyElem.getAsJsonObject() : null;

            String action = headers.get("action").getAsString();
            if (action == null || action.isEmpty()) {
                sendError(writer, 400, "Missing action in headers");
                return;
            }

            String controllerKey = action.contains("/") ? action.split("/")[0] : action;
            Controller controller = controllerFactory.getController(controllerKey);

            if (controller == null) {
                sendError(writer, 404, "No controller found for action: " + action);
                return;
            }

            Request<JsonObject> request = new Request<>();
            java.util.Map<String, String> headerMap = new java.util.HashMap<>();
            for (String key : headers.keySet()) {
                headerMap.put(key, headers.get(key).getAsString());
            }
            request.setHeaders(headerMap);
            request.setBody(body);

            Response<?> response = controller.handle(request);
            String jsonResponse = gson.toJson(response);

            writer.println(jsonResponse);
            writer.flush();

        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            try (PrintWriter errorWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                sendError(errorWriter, 500, "Internal server error: " + e.getMessage());
            } catch (IOException ioEx) {
                System.err.println("Failed to send error response: " + ioEx.getMessage());
            }
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void sendError(PrintWriter writer, int statusCode, String message) {
        Response<String> errorResponse = new Response<>(statusCode, message, null);
        writer.println(gson.toJson(errorResponse));
        writer.flush();
    }
}
