package com.hit.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class InteractiveClient {

    private static final String HOST = "localhost";
    private static final int FLIGHT_PORT = 3001;
    private static final int BOOKING_PORT = 3002;
    private static final int USER_PORT = 3003;

    private final Scanner userInput;

    public InteractiveClient() {
        this.userInput = new Scanner(System.in);
    }

    public void start() {
        System.out.println("========================================");
        System.out.println("     Flight Booking System - Client");
        System.out.println("========================================\n");

        while (true) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = userInput.nextLine().trim();

            switch (choice) {
                case "1": registerUser(); break;
                case "2": loginUser(); break;
                case "3": bookFlight(); break;
                case "4": getTicket(); break;
                case "5": cancelFlight(); break;
                case "6": calculateRoute(); break;
                case "7":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.\n");
            }
        }
    }

    private void printMenu() {
        System.out.println("--- Menu ---");
        System.out.println("1.  Register user");
        System.out.println("2.  Login");
        System.out.println("3.  Book a flight");
        System.out.println("4.  Get ticket");
        System.out.println("5.  Cancel flight");
        System.out.println("6.  Calculate optimal route");
        System.out.println("7.  Exit\n");
    }

    private void registerUser() {
        System.out.print("Username: ");
        String username = userInput.nextLine();
        System.out.print("Password: ");
        String password = userInput.nextLine();
        System.out.print("Role (USER/ADMIN, default USER): ");
        String role = userInput.nextLine();
        if (role.isEmpty()) role = "USER";

        String json = String.format(
                "{\"headers\":{\"action\":\"user/register\"},\"body\":{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}}",
                username, password, role);
        sendAndPrint(USER_PORT, json);
    }

    private void loginUser() {
        System.out.print("Username: ");
        String username = userInput.nextLine();
        System.out.print("Password: ");
        String password = userInput.nextLine();

        String json = String.format(
                "{\"headers\":{\"action\":\"user/login\"},\"body\":{\"username\":\"%s\",\"password\":\"%s\"}}",
                username, password);
        sendAndPrint(USER_PORT, json);
    }

    private void bookFlight() {
        System.out.print("Ticket ID: ");
        String id = userInput.nextLine();
        System.out.print("Customer name: ");
        String name = userInput.nextLine();
        System.out.print("Source: ");
        String source = userInput.nextLine();
        System.out.print("Destination: ");
        String dest = userInput.nextLine();
        System.out.print("Price: ");
        String price = userInput.nextLine();

        String json = String.format(
                "{\"headers\":{\"action\":\"booking/create\"},\"body\":{\"id\":\"%s\",\"customerName\":\"%s\",\"source\":\"%s\",\"destination\":\"%s\",\"price\":%s}}",
                id, name, source, dest, price);
        sendAndPrint(BOOKING_PORT, json);
    }

    private void getTicket() {
        System.out.print("Ticket ID: ");
        String id = userInput.nextLine();

        String json = String.format(
                "{\"headers\":{\"action\":\"flight/get\"},\"body\":{\"id\":\"%s\"}}", id);
        sendAndPrint(FLIGHT_PORT, json);
    }

    private void cancelFlight() {
        System.out.print("Ticket ID: ");
        String id = userInput.nextLine();

        String json = String.format(
                "{\"headers\":{\"action\":\"booking/cancel\"},\"body\":{\"id\":\"%s\"}}", id);
        sendAndPrint(BOOKING_PORT, json);
    }

    private void calculateRoute() {
        System.out.println("Enter flight routes (city1,city2,weight). Empty line to finish.");
        StringBuilder graphJson = new StringBuilder("{");
        boolean first = true;
        while (true) {
            System.out.print("Route: ");
            String line = userInput.nextLine().trim();
            if (line.isEmpty()) break;
            String[] parts = line.split(",");
            if (parts.length < 3) {
                System.out.println("Format: city1,city2,weight (e.g. Paris,Lyon,100)");
                continue;
            }
            String from = parts[0].trim();
            String to = parts[1].trim();
            int weight = Integer.parseInt(parts[2].trim());
            if (!first) graphJson.append(",");
            graphJson.append(String.format("\"%s\":{\"%s\":%d}", from, to, weight));
            first = false;
        }
        graphJson.append("}");

        System.out.print("From city: ");
        String fromCity = userInput.nextLine();
        System.out.print("To city: ");
        String toCity = userInput.nextLine();

        String json = String.format(
                "{\"headers\":{\"action\":\"flight/route\"},\"body\":{\"graph\":%s,\"from\":\"%s\",\"to\":\"%s\"}}",
                graphJson.toString(), fromCity, toCity);
        sendAndPrint(FLIGHT_PORT, json);
    }

    private void sendAndPrint(int port, String jsonRequest) {
        System.out.println("\n--- Request ---");
        System.out.println(jsonRequest);
        System.out.println("Sending to port " + port + "...");

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

            System.out.println("--- Response ---");
            System.out.println(formatJson(response.toString()));

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        System.out.println();
    }

    private String formatJson(String json) {
        return json.replace(",", ",\n  ").replace("{", "{\n  ").replace("}", "\n}").replace("\":\"", "\" : \"");
    }

    public static void main(String[] args) {
        new InteractiveClient().start();
    }
}
