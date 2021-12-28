package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public class BaseMessage implements Message {
    private final String header;
    private final String body;
    private Client client;

    public BaseMessage(String header, String body, Client client) {
        this.header = header;
        this.body = body;
        this.client = client;
    }

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public void setSender(Client client) {
        this.client = client;
    }

    public String getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    public Client getSender() {
        return this.client;
    }

    @Override
    public String toString() {
        return header + " " + body;
    }
}
