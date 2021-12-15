package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public interface Message {
    String getHeader();
    String getBody();
    Client getClient();
    void setClient(Client client);
    void accept(MessageHandler messageHandler);
}
