package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public class BaseMessage implements Message {
    private final String header;
    private final Client sender;
    private final String body;

    public BaseMessage(String header, String body, Client sender) {
        this.header = header;
        this.body = body;
        this.sender = sender;
    }

    public String getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    public Client getSender() {
        return this.sender;
    }

    @Override
    public String toString() {
        return header + " " + body;
    }
}
