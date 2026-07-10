package com.hit.client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hit.client.model.FlightData;
import com.hit.client.model.ServerClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    @FXML private Circle statusDot;
    @FXML private Label statusLabel;
    @FXML private Label statFlights;

    // Navigation
    @FXML private VBox dashboardView, flightView, routeView, bookingView, userView;
    @FXML private Button navDashboard, navFlights, navRoute, navBooking, navUser;

    // Flight search
    @FXML private TextField searchIdField;
    @FXML private TableView<FlightData> flightsTable;
    @FXML private TableColumn<FlightData, String> colId, colCustomer, colSource, colDest, colPrice;
    @FXML private TextArea flightResultArea;

    // Route planner
    @FXML private TextField routeFrom, routeTo, edgeInput;
    @FXML private TextArea edgeList, routeResultArea;

    // Booking
    @FXML private TextField bookId, bookCustomer, bookSource, bookDest, bookPrice;
    @FXML private TextField cancelId;
    @FXML private TextArea bookingResultArea;

    // User account
    @FXML private TextField regUsername, regPassword, regRole;
    @FXML private TextField loginUsername, loginPassword;
    @FXML private TextArea userResultArea;
    @FXML private Label userStatusLabel;

    @FXML private ProgressBar progressBar;

    private final ServerClient client = new ServerClient();
    private final Gson gson = new Gson();
    private final ObservableList<FlightData> flightDataList = FXCollections.observableArrayList();
    private final Map<String, Map<String, Integer>> graphEdges = new LinkedHashMap<>();
    private final List<String> edgeDisplay = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colSource.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSource()));
        colDest.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDestination()));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPrice())));
        flightsTable.setItems(flightDataList);
        selectNav(navDashboard);
    }

    // ===== Navigation =====

    @FXML private void onDashboard() { showView(dashboardView, navDashboard); }
    @FXML private void onFlightSearch() { showView(flightView, navFlights); }
    @FXML private void onRoutePlanner() { showView(routeView, navRoute); }
    @FXML private void onBookings() { showView(bookingView, navBooking); }
    @FXML private void onUserAccount() { showView(userView, navUser); }

    private void showView(VBox view, Button btn) {
        List.of(dashboardView, flightView, routeView, bookingView, userView).forEach(v -> {
            v.setVisible(false);
            v.setManaged(false);
        });
        view.setVisible(true);
        view.setManaged(true);
        selectNav(btn);
    }

    private void selectNav(Button selected) {
        List.of(navDashboard, navFlights, navRoute, navBooking, navUser).forEach(b -> b.getStyleClass().remove("selected"));
        selected.getStyleClass().add("selected");
    }

    // ===== Flight Search =====

    @FXML private void onSearchFlight() {
        String id = searchIdField.getText().trim();
        if (id.isEmpty()) {
            flightResultArea.setText("Please enter a Ticket ID.");
            return;
        }
        JsonObject req = buildRequest("flight/get", id);
        sendRequest(client.getFlightPort(), req, flightResultArea, result -> {
            flightDataList.clear();
            FlightData fd = parseFlight(result);
            if (fd != null) flightDataList.add(fd);
        });
    }

    @FXML private void onRefreshFlights() {
        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", "flight/getall");
        req.add("headers", headers);
        req.add("body", new JsonObject());
        sendRequest(client.getFlightPort(), req, flightResultArea, result -> {
            flightDataList.clear();
            List<FlightData> list = parseFlightList(result);
            flightDataList.addAll(list);
            statFlights.setText(String.valueOf(list.size()));
        });
    }

    // ===== Route Planner =====

    @FXML private void onAddEdge() {
        String input = edgeInput.getText().trim();
        if (input.isEmpty()) return;
        String[] parts = input.split(",");
        if (parts.length != 3) {
            routeResultArea.setText("Invalid format. Use: city1,city2,weight");
            return;
        }
        String from = parts[0].trim();
        String to = parts[1].trim();
        int weight;
        try { weight = Integer.parseInt(parts[2].trim()); } catch (NumberFormatException e) {
            routeResultArea.setText("Weight must be a number.");
            return;
        }
        graphEdges.computeIfAbsent(from, k -> new LinkedHashMap<>()).put(to, weight);
        edgeDisplay.add(String.format("%s -> %s ($%d)", from, to, weight));
        edgeList.setText(String.join("\n", edgeDisplay));
        edgeInput.clear();
        routeResultArea.setText(String.format("Edge added: %s -> %s (weight %d)", from, to, weight));
    }

    @FXML private void onShortestRoute() {
        calculateRoute();
    }

    @FXML private void onCheapestRoute() {
        calculateRoute();
    }

    @FXML private void onClearRoute() {
        graphEdges.clear();
        edgeDisplay.clear();
        edgeList.setText("No edges added yet.");
        routeFrom.clear();
        routeTo.clear();
        routeResultArea.clear();
    }

    private void calculateRoute() {
        String from = routeFrom.getText().trim();
        String to = routeTo.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            routeResultArea.setText("Please enter both 'From' and 'To' cities.");
            return;
        }
        if (graphEdges.isEmpty()) {
            routeResultArea.setText("No edges in graph. Add edges first.");
            return;
        }
        JsonObject graphJson = new JsonObject();
        for (Map.Entry<String, Map<String, Integer>> entry : graphEdges.entrySet()) {
            JsonObject neighbors = new JsonObject();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                neighbors.addProperty(edge.getKey(), edge.getValue());
            }
            graphJson.add(entry.getKey(), neighbors);
        }

        JsonObject body = new JsonObject();
        body.add("graph", graphJson);
        body.addProperty("from", from);
        body.addProperty("to", to);

        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", "flight/route");
        req.add("headers", headers);
        req.add("body", body);

        sendRequest(client.getFlightPort(), req, routeResultArea, null);
    }

    // ===== Booking =====

    @FXML private void onBookFlight() {
        String id = bookId.getText().trim();
        String cust = bookCustomer.getText().trim();
        String src = bookSource.getText().trim();
        String dest = bookDest.getText().trim();
        String priceStr = bookPrice.getText().trim();
        if (id.isEmpty() || cust.isEmpty() || src.isEmpty() || dest.isEmpty() || priceStr.isEmpty()) {
            bookingResultArea.setText("Please fill in all booking fields.");
            return;
        }
        int price;
        try { price = Integer.parseInt(priceStr); } catch (NumberFormatException e) {
            bookingResultArea.setText("Price must be a number.");
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("id", id);
        body.addProperty("customerName", cust);
        body.addProperty("source", src);
        body.addProperty("destination", dest);
        body.addProperty("price", price);
        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", "booking/create");
        req.add("headers", headers);
        req.add("body", body);
        sendRequest(client.getBookingPort(), req, bookingResultArea, result -> {
            bookId.clear(); bookCustomer.clear(); bookSource.clear(); bookDest.clear(); bookPrice.clear();
            onRefreshFlights();
        });
    }

    @FXML private void onCancelFlight() {
        String id = cancelId.getText().trim();
        if (id.isEmpty()) {
            bookingResultArea.setText("Enter a Ticket ID to cancel.");
            return;
        }
        JsonObject req = buildRequest("booking/cancel", id);
        sendRequest(client.getBookingPort(), req, bookingResultArea, result -> {
            cancelId.clear();
            onRefreshFlights();
        });
    }

    // ===== User Account =====

    @FXML private void onRegister() {
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();
        String role = regRole.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            userResultArea.setText("Please enter username and password.");
            return;
        }
        if (role.isEmpty()) role = "USER";

        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        body.addProperty("role", role);

        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", "user/register");
        req.add("headers", headers);
        req.add("body", body);

        sendRequest(client.getUserPort(), req, userResultArea, result -> {
            Platform.runLater(() -> {
                regUsername.clear();
                regPassword.clear();
                regRole.clear();
            });
        });
    }

    @FXML private void onLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            userResultArea.setText("Please enter username and password.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", "user/login");
        req.add("headers", headers);
        req.add("body", body);

        sendRequest(client.getUserPort(), req, userResultArea, result -> {
            Platform.runLater(() -> {
                loginUsername.clear();
                loginPassword.clear();
            });
        });
    }

    // ===== Helpers =====

    private JsonObject buildRequest(String action, String bodyValue) {
        JsonObject req = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("action", action);
        req.add("headers", headers);
        JsonObject body = new JsonObject();
        body.addProperty("id", bodyValue);
        req.add("body", body);
        return req;
    }

    private void sendRequest(int port, JsonObject request, TextArea outputArea, java.util.function.Consumer<String> onSuccess) {
        Platform.runLater(() -> {
            progressBar.setVisible(true);
            outputArea.setText("Sending request...");
        });
        Task<String> task = client.sendRequestAsync(port, request);
        task.setOnSucceeded(e -> {
            String result = task.getValue();
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                outputArea.setText(formatJson(result));
                if (onSuccess != null) onSuccess.accept(result);
            });
        });
        task.setOnFailed(e -> Platform.runLater(() -> {
            progressBar.setVisible(false);
            outputArea.setText("Error: " + task.getException().getMessage());
        }));
        new Thread(task).start();
    }

    private FlightData parseFlight(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj == null) return null;
            JsonObject data = obj.getAsJsonObject("data");
            if (data == null) return null;
            return new FlightData(
                    getString(data, "id"),
                    getString(data, "customerName"),
                    getString(data, "source"),
                    getString(data, "destination"),
                    getInt(data, "price")
            );
        } catch (Exception e) {
            return null;
        }
    }

    private List<FlightData> parseFlightList(String json) {
        List<FlightData> list = new ArrayList<>();
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj == null) return list;
            JsonArray arr = obj.getAsJsonArray("data");
            if (arr == null) return list;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject d = arr.get(i).getAsJsonObject();
                list.add(new FlightData(
                        getString(d, "id"),
                        getString(d, "customerName"),
                        getString(d, "source"),
                        getString(d, "destination"),
                        getInt(d, "price")
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int getInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }

    private String formatJson(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj != null) {
                int code = obj.has("statusCode") ? obj.get("statusCode").getAsInt() : 0;
                String msg = obj.has("message") ? obj.get("message").getAsString() : "";
                Object data = obj.has("data") && !obj.get("data").isJsonNull() ? obj.get("data") : null;
                StringBuilder sb = new StringBuilder();
                sb.append("Status: ").append(code).append(" | ").append(msg);
                if (data != null) {
                    sb.append("\nData: ").append(gson.toJson(data));
                }
                return sb.toString();
            }
        } catch (Exception ignored) {}
        return json;
    }
}
