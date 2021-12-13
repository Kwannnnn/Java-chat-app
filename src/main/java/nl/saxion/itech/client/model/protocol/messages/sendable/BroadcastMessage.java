package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;

public class BroadcastMessage implements Message {
    private static final String HEADER = "BCST";
    private String body;

    public BroadcastMessage(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
