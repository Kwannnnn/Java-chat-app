package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public interface Message {
    String getHeader();
    String getBody();
    Client getSender();
    void setSender(Client sender);
    void accept(MessageHandler messageHandler);
}
