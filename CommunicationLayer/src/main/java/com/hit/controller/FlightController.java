package com.hit.controller;

import com.google.gson.JsonObject;
import com.hit.server.Request;
import com.hit.server.Response;
import com.hit.service.FlightBookingService;

import java.util.List;
import java.util.Map;

public class FlightController implements Controller {

    private final FlightBookingService flightBookingService;

    public FlightController(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }

    @Override
    public Response<?> handle(Request<?> request) {
        String action = request.getHeaders().get("action");
        String subAction = action.contains("/") ? action.split("/", 2)[1] : "";

        switch (subAction) {
            case "getall":
                return handleGetAll();
            case "get":
                return handleGet((JsonObject) request.getBody());
            case "route":
                return handleRoute((JsonObject) request.getBody());
            default:
                return new Response<>(404, "Unknown flight action: " + action, null);
        }
    }

    public Response<?> handleGetAll() {
        return new Response<>(200, "All tickets", flightBookingService.getAllTickets());
    }

    public Response<?> handleGet(JsonObject body) {
        String id = body != null ? body.get("id").getAsString() : null;
        if (id == null || id.isEmpty()) {
            return new Response<>(400, "Missing ticket id", null);
        }
        Object ticket = flightBookingService.getTicket(id);
        if (ticket == null) {
            return new Response<>(404, "Ticket not found: " + id, null);
        }
        return new Response<>(200, "Ticket found", ticket);
    }

    public Response<?> handleRoute(JsonObject body) {
        if (body == null || !body.has("graph") || !body.has("from") || !body.has("to")) {
            return new Response<>(400, "Missing graph, from, or to in body", null);
        }
        Map<String, Map<String, Integer>> graph = gsonToGraph(body.getAsJsonObject("graph"));
        String from = body.get("from").getAsString();
        String to = body.get("to").getAsString();
        List<String> route = flightBookingService.getOptimalRoute(graph, from, to);
        return new Response<>(200, "Route calculated", route);
    }

    private Map<String, Map<String, Integer>> gsonToGraph(JsonObject jsonGraph) {
        Map<String, Map<String, Integer>> graph = new java.util.LinkedHashMap<>();
        for (String src : jsonGraph.keySet()) {
            JsonObject neighbors = jsonGraph.getAsJsonObject(src);
            Map<String, Integer> neighborMap = new java.util.LinkedHashMap<>();
            for (String dst : neighbors.keySet()) {
                neighborMap.put(dst, neighbors.get(dst).getAsInt());
            }
            graph.put(src, neighborMap);
        }
        return graph;
    }
}
