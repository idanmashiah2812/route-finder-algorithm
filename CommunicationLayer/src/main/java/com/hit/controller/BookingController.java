package com.hit.controller;

import com.google.gson.JsonObject;
import com.hit.dm.FlightTicket;
import com.hit.server.Request;
import com.hit.server.Response;
import com.hit.service.FlightBookingService;

public class BookingController implements Controller {

    private final FlightBookingService flightBookingService;

    public BookingController(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }

    @Override
    public Response<?> handle(Request<?> request) {
        String action = request.getHeaders().get("action");
        String subAction = action.contains("/") ? action.split("/", 2)[1] : "";

        switch (subAction) {
            case "create":
                return handleCreate((JsonObject) request.getBody());
            case "cancel":
                return handleCancel((JsonObject) request.getBody());
            default:
                return new Response<>(404, "Unknown booking action: " + action, null);
        }
    }

    public Response<?> handleCreate(JsonObject body) {
        if (body == null || !body.has("id") || !body.has("customerName")
                || !body.has("source") || !body.has("destination") || !body.has("price")) {
            return new Response<>(400, "Missing required ticket fields", null);
        }
        String id = body.get("id").getAsString();
        if (flightBookingService.getTicket(id) != null) {
            return new Response<>(409, "Ticket already exists: " + id, null);
        }
        FlightTicket ticket = new FlightTicket(
                id,
                body.get("customerName").getAsString(),
                body.get("source").getAsString(),
                body.get("destination").getAsString(),
                body.get("price").getAsInt()
        );
        flightBookingService.bookFlight(ticket);
        return new Response<>(201, "Booking created", ticket);
    }

    public Response<?> handleCancel(JsonObject body) {
        String id = body != null ? body.get("id").getAsString() : null;
        if (id == null || id.isEmpty()) {
            return new Response<>(400, "Missing booking id", null);
        }
        flightBookingService.cancelFlight(id);
        return new Response<>(200, "Booking cancelled", null);
    }
}
