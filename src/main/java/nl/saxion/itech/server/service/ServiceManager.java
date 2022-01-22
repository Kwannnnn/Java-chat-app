package nl.saxion.itech.server.service;

import nl.saxion.itech.server.thread.ServiceListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServiceManager {
    private final Map<Integer, ServiceListener> runningService; // HashMap mapping ports to listeners

    public ServiceManager() {
        this.runningService = new HashMap<>();
    }

    public void addService(Service service, int port)
            throws IOException {
        // Check if there is already a service running on that port
        if (this.runningService.containsKey(port))
            throw new IllegalArgumentException("Port " + port
                    + " already in use.");
        // Create a listener to handle connections on that port
        var listener = new ServiceListener(service, port);
        // Store the service in the server state
        this.runningService.put(port, listener);
        // Start the listener running.
        listener.start();

    }
}
