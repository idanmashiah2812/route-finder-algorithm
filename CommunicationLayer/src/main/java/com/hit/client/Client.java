package com.hit.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String sendRequest(String jsonRequest) {
        try (
                Socket socket = new Socket(host, port);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
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
            return "{\"statusCode\":500,\"message\":\"Connection error: " + e.getMessage() + "\",\"data\":null}";
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java com.hit.client.Client <host> <port> <json>");
            System.out.println("Example:");
            System.out.println("  java com.hit.client.Client localhost 3001 \"{\\\"headers\\\":{\\\"action\\\":\\\"flight/get\\\"},\\\"body\\\":{\\\"id\\\":\\\"FT001\\\"}}\"");
            return;
        }

        Client client = new Client(args[0], Integer.parseInt(args[1]));
        String response = client.sendRequest(args[2]);
        System.out.println(response);
    }
}
