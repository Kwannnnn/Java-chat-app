package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.service.Service;

import java.io.IOException;
import java.net.Socket;

/**
 * This Thread subclass handles an individual connection between a client
 * and a Service provided by this server. This ensures that each service
 * can be provided to multiple connections at once. Thus, makes this a
 * multi-threaded server implementation.
 */
public class ClientThread extends Thread {
    private final Socket socket; // The client object
    private final Service service; // The service being provided to that client

    public ClientThread(Socket socket, Service service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try {
            var in = this.socket.getInputStream();
            var out = this.socket.getOutputStream();
            this.service.serve(in, out);
        } catch (IOException e) {
            // TODO: think if it is necessary to do something here
        } finally {
            // Makes sure to stop the thread whenever the service
            // stops being provided
            closeConnection();
            this.interrupt();
        }
    }

    private void closeConnection() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // Connection has already been closed
        }
    }
}
