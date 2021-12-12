package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class BroadcastMessage implements SendableMessage {
    private static final String HEADER = "BCST";
    private String body;

    public BroadcastMessage(String body) {
        this.body = body;
    }

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
