package nl.saxion.itech.server;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.service.Service;
import nl.saxion.itech.server.service.ServiceManager;
import nl.saxion.itech.server.util.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is a generic framework for a flexible, multi-threaded server. It
 * listens on any number of specified ports, and, when it receives a connection
 * on a port, passes input and output streams to a specified Service object
 * which provides the actual service. It can limit the number of concurrent
 * connections, and logs activity to a specified stream.
 */
public class Server {
    // Internal state of the server
    private final DataObject data;
    private final ServiceManager serviceManager;
    private Logger logger;

    /**
     * The server takes a data object to keep state of connected/authenticated
     * users, available groups, etc.
     * @param data Non-null DataObject
     */
    public Server(DataObject data, OutputStream logStream) {
        assert data != null : "Server has not been provided with data";

        this.data = data;
        this.serviceManager = new ServiceManager();

        if (logStream != null) {
            this.logger = Logger.getInstance();
            this.logger.init(logStream);
            this.logger.logMessage("Logger initiated");
        }
    }

    /**
     * A method that enables the server to provide a new service on a
     * specified port.
     * @param service the new service
     * @param port the port to run the service on
     * @throws IOException if an I/O error occurs while working with
     * the socket
     */
    protected void addService(Service service, int port)
            throws IOException {
        this.serviceManager.addService(service, port);
        this.logger.logMessage(service.getClass().getSimpleName()
                + " service listening on port " + port);
    }
 }
