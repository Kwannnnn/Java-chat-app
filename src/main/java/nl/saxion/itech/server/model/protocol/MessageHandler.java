package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public interface MessageHandler {
    void handle(String message, Client sender);
}
