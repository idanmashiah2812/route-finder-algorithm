package com.hit.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;

import java.io.*;
import java.net.Socket;

public class ServerClient {

    private static final String HOST = "localhost";
    private final Gson gson;

    public ServerClient() {
        this.gson = new Gson();
    }

    public Task<String> sendRequestAsync(int port, JsonObject request) {
        return new Task<String>() {
            @Override
            protected String call() {
                String jsonRequest = gson.toJson(request);
                try (Socket socket = new Socket(HOST, port);
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    writer.println(jsonRequest);
                    writer.println();
                    writer.flush();

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();

                } catch (IOException e) {
                    return String.format("{\"statusCode\":500,\"message\":\"%s\",\"data\":null}",
                            e.getMessage().replace("\"", "'"));
                }
            }
        };
    }

    public int getFlightPort() { return 3001; }
    public int getBookingPort() { return 3002; }
    public int getUserPort() { return 3003; }
}
