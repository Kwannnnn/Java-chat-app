package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public class BaseMessage implements Message {
    private final String header;
    private Client client;
    private String body;

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public BaseMessage(String header, String body, Client sender) {
        this.header = header;
        this.body = body;
        this.client = sender;
    }

    public String getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void accept(MessageHandler messageHandler) {
        messageHandler.handle(this);
    }

    @Override
    public String toString() {
        return header + " " + body;
    }
}
