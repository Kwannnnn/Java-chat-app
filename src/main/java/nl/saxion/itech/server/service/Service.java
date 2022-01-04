package nl.saxion.itech.server.service;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This Service interface defines only a single method which is invoked
 * to provide that particular service.
 *
 * Each connection through the same port share the same Service object.
 * Which means that any state regarding an individual connection must be
 * stored in a local variable withing the serve() method. Any shared state
 * can be stored as an instance variable of the Service class.
 */
public interface Service {

    /**
     * Provides a service to a specific input and output streams.
     *
     * @param in the InputStream to provide the service to
     * @param out the OutputStream to provide the service to
     * the socket
     */
    void serve(InputStream in, OutputStream out);
}
