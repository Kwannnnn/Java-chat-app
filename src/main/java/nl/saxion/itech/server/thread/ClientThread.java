package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.service.Service;

import java.net.Socket;

/**
 * This Thread subclass handles an individual connection between a client
 * and a Service provided by this server. This ensures that each service
 * can be provided to multiple connections at once. Thus, makes this a
 * multi-threaded server implementation.
 */
public class ClientThread extends Thread {
    private final Client client; // The client object
    private final Service service; // The service being provided to that client

    public ClientThread(Socket socket, Service service) {
        this.client = new Client(socket);
        this.service = service;
    }

    @Override
    public void run() {
        this.service.serve(this.client);
        // Makes sure to stop the thread whenever the service
        // stops being provided
        this.interrupt();
    }
}
