package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;

/**
 * This Service interface defines only a single method which is invoked
 * to provide that particular service.
 *
 * Each connection through the same port share the same Service object.
 * Which means that any state regarding an individual connection must be
 * stored in a local variable withing the serve() method. Any shared state
 * can be stored as an instance variable of the Service class.
 *
 * NOTE: all input/output streams must be closed, before stopping providing
 * a service to a client.
 */
public interface Service {

    /**
     * Provides a service to a client. Upon returning the service
     * must close the client socket!
     *
     * @param client the Client object to provide the service to
     * the socket
     */
    void serve(Client client);
}
