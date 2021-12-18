package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;

public class BaseMessage implements Message {
    private final String header;
    private Client sender;
    private String body;

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

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

    public void setSender(Client sender) {
        this.sender = sender;
    }

    @Override
    public void accept(MessageHandler messageHandler) {
//        messageHandler.handle(this);
    }

    @Override
    public String toString() {
        return header + " " + body;
    }
}
