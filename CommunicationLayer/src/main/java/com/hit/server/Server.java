package com.hit.server;

import com.hit.util.ControllerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final int port;
    private final ControllerFactory controllerFactory;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final ExecutorService threadPool;

    public Server(int port, ControllerFactory controllerFactory) {
        this.port = port;
        this.controllerFactory = controllerFactory;
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server listening on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new HandleRequest(clientSocket, controllerFactory));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting connection on port " + port + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Could not start server on port " + port + ": " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        threadPool.shutdown();
    }
}
