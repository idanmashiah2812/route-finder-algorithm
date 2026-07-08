package com.hit.server;

import com.hit.util.ControllerFactory;

public class ServerDriver {

    private static final int FLIGHT_SERVER_PORT = 3001;
    private static final int BOOKING_SERVER_PORT = 3002;
    private static final int USER_SERVER_PORT = 3003;

    public static void main(String[] args) {
        ControllerFactory controllerFactory = new ControllerFactory();

        Server flightServer = new Server(FLIGHT_SERVER_PORT, controllerFactory);
        Server bookingServer = new Server(BOOKING_SERVER_PORT, controllerFactory);
        Server userServer = new Server(USER_SERVER_PORT, controllerFactory);

        Thread flightThread = new Thread(flightServer, "FlightServer-3001");
        Thread bookingThread = new Thread(bookingServer, "BookingServer-3002");
        Thread userThread = new Thread(userServer, "UserServer-3003");

        flightThread.start();
        bookingThread.start();
        userThread.start();

        System.out.println("All servers started:");
        System.out.println("  FlightServer   -> port " + FLIGHT_SERVER_PORT);
        System.out.println("  BookingServer  -> port " + BOOKING_SERVER_PORT);
        System.out.println("  UserServer     -> port " + USER_SERVER_PORT);
        System.out.println("Press Ctrl+C to stop all servers.");
    }
}
