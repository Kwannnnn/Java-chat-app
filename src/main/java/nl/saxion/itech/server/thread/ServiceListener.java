package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.service.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * This Thread subclass uses a ServerSocket to listen for connections on a
 * specified port (using a ServerSocket) and when it gets a connection
 * request. Each Service provided by the server has its own ServiceListener.
 */
public class ServiceListener extends Thread {
    private final ServerSocket serverSocket; // The socket to listen for connections
    private final Service service; // The service to provide on that port
    private volatile boolean isRunning = true;

    /**
     * The ServiceListener constructor creates a ServerSocket to listen for
     * connections on the specified port.
     */
    public ServiceListener(Service service, int port) throws IOException {
        this.service = service;
        this.serverSocket = new ServerSocket(port);
    }

    /**
     * This method closes the server socket and causes the ServiceListener
     * to stop accepting new connections.
     */
    public void stopListening() {
        this.isRunning = false; // Set the stop flag
        this.interrupt(); // Stop running this and all child threads
        try {
            this.serverSocket.close();
        } // Stop listening.
        catch (IOException e) {
        }
    }

    @Override
    public void run() {
        while (this.isRunning) { // loop until we're asked to stop.
            try {
                var socket = this.serverSocket.accept();
                new ClientThread(socket, this.service).start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
