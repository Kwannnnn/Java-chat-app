package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class BroadcastMessage implements SendableMessage {
    private static final String HEADER = "BCST";
    private String message;

    public BroadcastMessage(String message) {
        this.message = message;
    }

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
