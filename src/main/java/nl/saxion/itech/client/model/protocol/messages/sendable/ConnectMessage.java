package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;

public class ConnectMessage implements Message {
    private static final String HEADER = "CONN";
    private String body;

    public ConnectMessage(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
